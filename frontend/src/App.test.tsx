import { render, screen } from '@testing-library/react';
import { App } from './App';

describe('App shell', () => {
  it('renders the application title and welcome content', () => {
    render(<App />);

    expect(screen.getByRole('link', { name: 'Support Ticket Management' })).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: 'Welcome' })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'View Tickets' })).toBeInTheDocument();
  });
});
