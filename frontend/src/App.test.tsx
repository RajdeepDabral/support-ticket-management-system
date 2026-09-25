import { render, screen } from '@testing-library/react';
import { App } from './App';

describe('App shell', () => {
  it('renders the application title and welcome content', () => {
    render(<App />);

    expect(screen.getByRole('heading', { name: 'Support Ticket Management' })).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: 'Welcome' })).toBeInTheDocument();
    expect(screen.getByText(/frontend foundation is ready/i)).toBeInTheDocument();
  });
});
