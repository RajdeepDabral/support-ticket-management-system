package com.supportticket.acceptance;

import static org.assertj.core.api.Assertions.assertThat;
import com.jayway.jsonpath.JsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.OffsetDateTime;
import java.util.stream.Stream;

import com.supportticket.domain.Ticket;
import com.supportticket.domain.TicketPriority;
import com.supportticket.domain.TicketStatus;
import com.supportticket.repository.CommentRepository;
import com.supportticket.repository.TicketRepository;
import com.supportticket.support.AbstractPostgreSQLContainerTest;
import com.supportticket.support.IntegrationTestSupport;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Support Ticket Backend Acceptance")
class SupportTicketBackendAcceptanceTest extends AbstractPostgreSQLContainerTest {

    private static final String TICKETS_URL = IntegrationTestSupport.TICKETS_URL;
    private static final long MISSING_TICKET_ID = 9_999_999L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private CommentRepository commentRepository;

    @BeforeEach
    void cleanDatabase() {
        commentRepository.deleteAll();
        ticketRepository.deleteAll();
    }

    @Nested
    @DisplayName("Ticket lifecycle")
    class TicketLifecycle {

        @Test
        void createRetrieveUpdateAndPersistThroughFullStack() throws Exception {
            String createResponse = mockMvc.perform(post(TICKETS_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "title": "Unable to login",
                                      "description": "User receives an authentication error.",
                                      "priority": "HIGH",
                                      "assignee": "john.doe"
                                    }
                                    """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.status").value("OPEN"))
                    .andExpect(jsonPath("$.createdAt").exists())
                    .andExpect(jsonPath("$.updatedAt").exists())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            Long ticketId = extractId(createResponse);

            mockMvc.perform(get(IntegrationTestSupport.ticketUrl(ticketId)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(ticketId))
                    .andExpect(jsonPath("$.title").value("Unable to login"))
                    .andExpect(jsonPath("$.description").value("User receives an authentication error."))
                    .andExpect(jsonPath("$.priority").value("HIGH"))
                    .andExpect(jsonPath("$.status").value("OPEN"))
                    .andExpect(jsonPath("$.assignee").value("john.doe"));

            Ticket beforeUpdate = ticketRepository.findById(ticketId).orElseThrow();
            OffsetDateTime createdAt = beforeUpdate.getCreatedAt();
            OffsetDateTime updatedAtBefore = beforeUpdate.getUpdatedAt();

            mockMvc.perform(patch(IntegrationTestSupport.ticketUrl(ticketId))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "title": "Unable to login after reset",
                                      "description": "Updated after password reset.",
                                      "priority": "CRITICAL",
                                      "assignee": "jane.doe"
                                    }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("Unable to login after reset"))
                    .andExpect(jsonPath("$.description").value("Updated after password reset."))
                    .andExpect(jsonPath("$.priority").value("CRITICAL"))
                    .andExpect(jsonPath("$.assignee").value("jane.doe"))
                    .andExpect(jsonPath("$.status").value("OPEN"));

            mockMvc.perform(get(IntegrationTestSupport.ticketUrl(ticketId)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("Unable to login after reset"))
                    .andExpect(jsonPath("$.assignee").value("jane.doe"));

            Ticket afterUpdate = ticketRepository.findById(ticketId).orElseThrow();
            assertThat(afterUpdate.getTitle()).isEqualTo("Unable to login after reset");
            assertThat(afterUpdate.getDescription()).isEqualTo("Updated after password reset.");
            assertThat(afterUpdate.getPriority()).isEqualTo(TicketPriority.CRITICAL);
            assertThat(afterUpdate.getAssignee()).isEqualTo("jane.doe");
            assertThat(afterUpdate.getStatus()).isEqualTo(TicketStatus.OPEN);
            assertThat(afterUpdate.getCreatedAt()).isEqualTo(createdAt);
            assertThat(afterUpdate.getUpdatedAt()).isAfterOrEqualTo(updatedAtBefore);
        }
    }

    @Nested
    @DisplayName("State machine")
    class StateMachine {

        @Test
        void progressesThroughValidLifecycleChain() throws Exception {
            Long ticketId = createTicket("Lifecycle chain", "Track valid transitions.");
            OffsetDateTime updatedAtBefore = ticketRepository.findById(ticketId).orElseThrow().getUpdatedAt();

            transitionStatus(ticketId, "IN_PROGRESS");
            assertPersistedStatus(ticketId, TicketStatus.IN_PROGRESS);
            OffsetDateTime updatedAtAfterFirstTransition =
                    ticketRepository.findById(ticketId).orElseThrow().getUpdatedAt();
            assertThat(updatedAtAfterFirstTransition).isAfterOrEqualTo(updatedAtBefore);

            transitionStatus(ticketId, "RESOLVED");
            assertPersistedStatus(ticketId, TicketStatus.RESOLVED);

            transitionStatus(ticketId, "CLOSED");
            assertPersistedStatus(ticketId, TicketStatus.CLOSED);
        }

        @ParameterizedTest(name = "{0} -> {1}")
        @MethodSource("com.supportticket.acceptance.SupportTicketBackendAcceptanceTest#validTransitions")
        void allowsValidTransitions(TicketStatus from, TicketStatus to) throws Exception {
            Long ticketId = createTicketWithStatus(from);

            transitionStatus(ticketId, to.name());

            assertPersistedStatus(ticketId, to);
        }

        @ParameterizedTest(name = "{0} -> {1} returns 409")
        @MethodSource("com.supportticket.acceptance.SupportTicketBackendAcceptanceTest#invalidTransitions")
        void rejectsInvalidTransitions(TicketStatus from, TicketStatus to) throws Exception {
            Long ticketId = createTicketWithStatus(from);

            mockMvc.perform(patch(IntegrationTestSupport.statusUrl(ticketId))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"status\": \"" + to + "\"}"))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value("INVALID_STATUS_TRANSITION"))
                    .andExpect(jsonPath("$.status").value(409))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.path").value(IntegrationTestSupport.statusUrl(ticketId)))
                    .andExpect(jsonPath("$.timestamp").exists());

            assertPersistedStatus(ticketId, from);
        }

        @ParameterizedTest
        @EnumSource(value = TicketStatus.class, names = {"CLOSED", "CANCELLED"})
        void terminalStatesRejectFurtherTransitions(TicketStatus terminalStatus) throws Exception {
            Long ticketId = createTicketWithStatus(terminalStatus);

            mockMvc.perform(patch(IntegrationTestSupport.statusUrl(ticketId))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"status\": \"OPEN\"}"))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value("INVALID_STATUS_TRANSITION"));

            assertPersistedStatus(ticketId, terminalStatus);
        }
    }

    @Nested
    @DisplayName("Comments")
    class Comments {

        @Test
        void createsCommentPersistsAssociationAndExposesOnTicketDetail() throws Exception {
            Long ticketId = createTicket("Login issue", "User cannot login.");

            mockMvc.perform(post(IntegrationTestSupport.commentsUrl(ticketId))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "content": "I have investigated the issue.",
                                      "author": "rajdeep"
                                    }
                                    """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.ticketId").value(ticketId))
                    .andExpect(jsonPath("$.content").value("I have investigated the issue."))
                    .andExpect(jsonPath("$.author").value("rajdeep"))
                    .andExpect(jsonPath("$.createdAt").exists());

            var comments = commentRepository.findByTicket_Id(ticketId);
            assertThat(comments).hasSize(1);
            assertThat(comments.get(0).getTicket().getId()).isEqualTo(ticketId);
            assertThat(comments.get(0).getCreatedAt()).isNotNull();

            mockMvc.perform(get(IntegrationTestSupport.ticketUrl(ticketId)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.comments.length()").value(1))
                    .andExpect(jsonPath("$.comments[0].content").value("I have investigated the issue."))
                    .andExpect(jsonPath("$.comments[0].author").value("rajdeep"));
        }
    }

    @Nested
    @DisplayName("Search and filter")
    class SearchAndFilter {

        @BeforeEach
        void seedSearchData() {
            ticketRepository.save(new Ticket(
                    "Login issue",
                    "User cannot access the application.",
                    TicketPriority.HIGH,
                    TicketStatus.OPEN,
                    "john.doe"));
            ticketRepository.save(new Ticket(
                    "API timeout",
                    "Unable to LOGIN during peak hours.",
                    TicketPriority.MEDIUM,
                    TicketStatus.IN_PROGRESS,
                    "jane.doe"));
            ticketRepository.save(new Ticket(
                    "Payment failure",
                    "Payment gateway returns an error.",
                    TicketPriority.CRITICAL,
                    TicketStatus.RESOLVED,
                    "alex.smith"));
            ticketRepository.save(new Ticket(
                    "Closed ticket",
                    "Resolved and closed.",
                    TicketPriority.LOW,
                    TicketStatus.CLOSED,
                    "ops.team"));
            ticketRepository.save(new Ticket(
                    "Cancelled ticket",
                    "Cancelled before work started.",
                    TicketPriority.MEDIUM,
                    TicketStatus.CANCELLED,
                    "reviewer"));
        }

        @Test
        void searchesTitleDescriptionCaseInsensitiveSubstringAndReturnsEmptyArray() throws Exception {
            mockMvc.perform(get(TICKETS_URL).param("keyword", "Login"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].title").value("Login issue"));

            mockMvc.perform(get(TICKETS_URL).param("keyword", "gateway"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].title").value("Payment failure"));

            mockMvc.perform(get(TICKETS_URL).param("keyword", "login"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2));

            mockMvc.perform(get(TICKETS_URL).param("keyword", "pay"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1));

            mockMvc.perform(get(TICKETS_URL).param("keyword", "nonexistent"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @ParameterizedTest
        @EnumSource(TicketStatus.class)
        void filtersBySupportedStatus(TicketStatus status) throws Exception {
            mockMvc.perform(get(TICKETS_URL).param("status", status.name()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].status").value(status.name()));
        }

        @Test
        void rejectsInvalidStatusFilter() throws Exception {
            mockMvc.perform(get(TICKETS_URL).param("status", "INVALID"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        }

        @Test
        void treatsEmptyAndWhitespaceKeywordAsNoFilter() throws Exception {
            mockMvc.perform(get(TICKETS_URL).param("keyword", ""))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(5));

            mockMvc.perform(get(TICKETS_URL).param("keyword", "   "))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(5));

            mockMvc.perform(get(TICKETS_URL).param("keyword", "  login  "))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2));
        }

        @Test
        void appliesCombinedKeywordAndStatusFilters() throws Exception {
            mockMvc.perform(get(TICKETS_URL).param("keyword", "login").param("status", "OPEN"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].title").value("Login issue"))
                    .andExpect(jsonPath("$[0].status").value("OPEN"));

            mockMvc.perform(get(TICKETS_URL).param("keyword", "login").param("status", "CLOSED"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    @Nested
    @DisplayName("Validation")
    class Validation {

        @ParameterizedTest(name = "{0}")
        @MethodSource("invalidTicketRequests")
        void rejectsInvalidTicketCreation(String scenario, String body) throws Exception {
            mockMvc.perform(post(TICKETS_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").exists())
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.path").value(TICKETS_URL))
                    .andExpect(jsonPath("$.timestamp").exists());

            assertThat(ticketRepository.findAll()).isEmpty();
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("invalidCommentRequests")
        void rejectsInvalidCommentCreation(String scenario, String body) throws Exception {
            Long ticketId = createTicket("Validation ticket", "For comment validation.");

            mockMvc.perform(post(IntegrationTestSupport.commentsUrl(ticketId))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

            assertThat(commentRepository.findByTicket_Id(ticketId)).isEmpty();
        }
    }

    @Nested
    @DisplayName("Not found")
    class NotFound {

        @ParameterizedTest(name = "{0}")
        @MethodSource("notFoundEndpoints")
        void returns404ForUnknownTicket(String scenario, String method, String url, String body) throws Exception {
            var request = switch (method) {
                case "GET" -> get(url);
                case "PATCH" -> patch(url).contentType(MediaType.APPLICATION_JSON).content(body);
                case "POST" -> post(url).contentType(MediaType.APPLICATION_JSON).content(body);
                default -> throw new IllegalArgumentException("Unsupported method: " + method);
            };

            mockMvc.perform(request)
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("TICKET_NOT_FOUND"))
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.path").value(url))
                    .andExpect(jsonPath("$.timestamp").exists());
        }
    }

    @Nested
    @DisplayName("API boundaries")
    class ApiBoundaries {

        @Test
        void rejectsClientControlledTicketFieldsOnCreate() throws Exception {
            mockMvc.perform(post(TICKETS_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "id": 999,
                                      "status": "CLOSED",
                                      "createdAt": "2020-01-01T00:00:00Z",
                                      "updatedAt": "2020-01-01T00:00:00Z",
                                      "title": "Boundary test",
                                      "description": "Client must not control backend fields.",
                                      "priority": "LOW",
                                      "assignee": "reviewer"
                                    }
                                    """))
                    .andExpect(status().isBadRequest());

            assertThat(ticketRepository.findAll()).isEmpty();
        }

        @Test
        void createsTicketWithBackendGeneratedIdentityAndOpenStatus() throws Exception {
            String response = mockMvc.perform(post(TICKETS_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "title": "Boundary test",
                                      "description": "Backend controls identity and status.",
                                      "priority": "LOW",
                                      "assignee": "reviewer"
                                    }
                                    """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.status").value("OPEN"))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            Long ticketId = extractId(response);
            assertThat(ticketId).isNotEqualTo(999L);

            Ticket persisted = ticketRepository.findById(ticketId).orElseThrow();
            assertThat(persisted.getStatus()).isEqualTo(TicketStatus.OPEN);
            assertThat(persisted.getCreatedAt()).isNotNull();
            assertThat(persisted.getUpdatedAt()).isNotNull();
        }

        @Test
        void rejectsClientControlledCommentFields() throws Exception {
            Long ticketId = createTicket("Comment boundary", "Verify comment API boundary.");
            Long otherTicketId = createTicket("Other ticket", "Different ticket.");

            mockMvc.perform(post(IntegrationTestSupport.commentsUrl(ticketId))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "id": 999,
                                      "ticketId": %d,
                                      "createdAt": "2020-01-01T00:00:00Z",
                                      "content": "Boundary comment",
                                      "author": "rajdeep"
                                    }
                                    """.formatted(otherTicketId)))
                    .andExpect(status().isBadRequest());

            assertThat(commentRepository.findByTicket_Id(ticketId)).isEmpty();
            assertThat(commentRepository.findByTicket_Id(otherTicketId)).isEmpty();
        }
    }

    @Nested
    @DisplayName("Persistence")
    class Persistence {

        @Test
        void survivesFullRequestLifecycleAcrossEndpoints() throws Exception {
            Long ticketId = createTicket("Persistence ticket", "Initial description.");

            transitionStatus(ticketId, "IN_PROGRESS");

            mockMvc.perform(patch(IntegrationTestSupport.ticketUrl(ticketId))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "title": "Persistence ticket updated",
                                      "assignee": "persist.user"
                                    }
                                    """))
                    .andExpect(status().isOk());

            mockMvc.perform(post(IntegrationTestSupport.commentsUrl(ticketId))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "content": "Persisted comment",
                                      "author": "reviewer"
                                    }
                                    """))
                    .andExpect(status().isCreated());

            ticketRepository.flush();
            ticketRepository.findById(ticketId).orElseThrow();
            commentRepository.findByTicket_Id(ticketId);

            mockMvc.perform(get(IntegrationTestSupport.ticketUrl(ticketId)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("Persistence ticket updated"))
                    .andExpect(jsonPath("$.assignee").value("persist.user"))
                    .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                    .andExpect(jsonPath("$.comments.length()").value(1))
                    .andExpect(jsonPath("$.comments[0].content").value("Persisted comment"));

            Ticket reloaded = ticketRepository.findById(ticketId).orElseThrow();
            assertThat(reloaded.getTitle()).isEqualTo("Persistence ticket updated");
            assertThat(reloaded.getAssignee()).isEqualTo("persist.user");
            assertThat(reloaded.getStatus()).isEqualTo(TicketStatus.IN_PROGRESS);
            assertThat(commentRepository.findByTicket_Id(ticketId)).hasSize(1);
        }
    }

    private Long createTicket(String title, String description) throws Exception {
        return createTicketWithStatus(title, description, TicketStatus.OPEN);
    }

    private Long createTicketWithStatus(TicketStatus status) throws Exception {
        return createTicketWithStatus("State machine ticket", "Ticket for status transition.", status);
    }

    private Long createTicketWithStatus(String title, String description, TicketStatus status) throws Exception {
        Ticket ticket = ticketRepository.save(new Ticket(
                title,
                description,
                TicketPriority.MEDIUM,
                status,
                "acceptance.test"));
        return ticket.getId();
    }

    private void transitionStatus(Long ticketId, String status) throws Exception {
        mockMvc.perform(patch(IntegrationTestSupport.statusUrl(ticketId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"" + status + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(status));
    }

    private void assertPersistedStatus(Long ticketId, TicketStatus expectedStatus) {
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow();
        assertThat(ticket.getStatus()).isEqualTo(expectedStatus);
    }

    private Long extractId(String json) {
        return JsonPath.parse(json).read("$.id", Long.class);
    }

    static Stream<Arguments> validTransitions() {
        return Stream.of(
                Arguments.of(TicketStatus.OPEN, TicketStatus.IN_PROGRESS),
                Arguments.of(TicketStatus.OPEN, TicketStatus.CANCELLED),
                Arguments.of(TicketStatus.IN_PROGRESS, TicketStatus.RESOLVED),
                Arguments.of(TicketStatus.IN_PROGRESS, TicketStatus.CANCELLED),
                Arguments.of(TicketStatus.RESOLVED, TicketStatus.CLOSED));
    }

    static Stream<Arguments> invalidTransitions() {
        return Stream.of(
                Arguments.of(TicketStatus.OPEN, TicketStatus.RESOLVED),
                Arguments.of(TicketStatus.OPEN, TicketStatus.CLOSED),
                Arguments.of(TicketStatus.IN_PROGRESS, TicketStatus.OPEN),
                Arguments.of(TicketStatus.IN_PROGRESS, TicketStatus.CLOSED),
                Arguments.of(TicketStatus.RESOLVED, TicketStatus.OPEN),
                Arguments.of(TicketStatus.RESOLVED, TicketStatus.IN_PROGRESS),
                Arguments.of(TicketStatus.CLOSED, TicketStatus.OPEN),
                Arguments.of(TicketStatus.CLOSED, TicketStatus.RESOLVED),
                Arguments.of(TicketStatus.CANCELLED, TicketStatus.OPEN),
                Arguments.of(TicketStatus.CANCELLED, TicketStatus.CLOSED));
    }

    static Stream<Arguments> invalidTicketRequests() {
        return Stream.of(
                Arguments.of(
                        "blank title",
                        """
                                {
                                  "title": "",
                                  "description": "Description",
                                  "priority": "HIGH",
                                  "assignee": "john.doe"
                                }
                                """),
                Arguments.of(
                        "blank description",
                        """
                                {
                                  "title": "Title",
                                  "description": "",
                                  "priority": "HIGH",
                                  "assignee": "john.doe"
                                }
                                """),
                Arguments.of(
                        "invalid priority",
                        """
                                {
                                  "title": "Title",
                                  "description": "Description",
                                  "priority": "URGENT",
                                  "assignee": "john.doe"
                                }
                                """),
                Arguments.of(
                        "blank assignee",
                        """
                                {
                                  "title": "Title",
                                  "description": "Description",
                                  "priority": "HIGH",
                                  "assignee": ""
                                }
                                """));
    }

    static Stream<Arguments> invalidCommentRequests() {
        return Stream.of(
                Arguments.of(
                        "blank content",
                        """
                                {
                                  "content": "",
                                  "author": "rajdeep"
                                }
                                """),
                Arguments.of(
                        "blank author",
                        """
                                {
                                  "content": "Some comment",
                                  "author": ""
                                }
                                """));
    }

    static Stream<Arguments> notFoundEndpoints() {
        String missingTicketUrl = IntegrationTestSupport.ticketUrl(MISSING_TICKET_ID);
        String missingStatusUrl = IntegrationTestSupport.statusUrl(MISSING_TICKET_ID);
        String missingCommentsUrl = IntegrationTestSupport.commentsUrl(MISSING_TICKET_ID);

        return Stream.of(
                Arguments.of("retrieve ticket", "GET", missingTicketUrl, null),
                Arguments.of(
                        "update ticket",
                        "PATCH",
                        missingTicketUrl,
                        """
                                {
                                  "title": "Updated"
                                }
                                """),
                Arguments.of(
                        "transition status",
                        "PATCH",
                        missingStatusUrl,
                        """
                                {
                                  "status": "IN_PROGRESS"
                                }
                                """),
                Arguments.of(
                        "add comment",
                        "POST",
                        missingCommentsUrl,
                        """
                                {
                                  "content": "Missing ticket comment",
                                  "author": "rajdeep"
                                }
                                """));
    }
}
