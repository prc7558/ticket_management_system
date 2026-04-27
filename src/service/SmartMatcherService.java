package service;

import model.Ticket;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Smart keyword-based matching engine (inspired by campus-issue CIRS MatchingService).
 *
 * Finds duplicate / related complaints by extracting keywords from titles and
 * descriptions, then computing the overlap between active tickets.
 */
public class SmartMatcherService {

    /** Common English stop-words to exclude from keyword matching. */
    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
        "the", "a", "an", "is", "are", "was", "were", "of", "and", "in", "on",
        "at", "to", "for", "with", "my", "this", "that", "it", "not", "but",
        "have", "has", "had", "been", "being", "do", "does", "did", "will",
        "would", "could", "should", "may", "might", "can", "shall", "about",
        "from", "into", "through", "during", "before", "after", "above",
        "below", "between", "same", "just", "very", "also", "too", "here",
        "there", "when", "where", "how", "all", "each", "every", "both",
        "few", "more", "most", "other", "some", "such", "than", "then",
        "issue", "problem", "complaint", "please", "help", "need", "want"
    ));

    /**
     * Represents a match between two tickets with a similarity score.
     */
    public static class TicketMatch {
        private final Ticket ticketA;
        private final Ticket ticketB;
        private final double score;
        private final Set<String> commonKeywords;

        public TicketMatch(Ticket a, Ticket b, double score, Set<String> commonKeywords) {
            this.ticketA = a;
            this.ticketB = b;
            this.score = score;
            this.commonKeywords = commonKeywords;
        }

        public Ticket getTicketA() { return ticketA; }
        public Ticket getTicketB() { return ticketB; }
        public double getScore() { return score; }
        public Set<String> getCommonKeywords() { return commonKeywords; }

        @Override
        public String toString() {
            return String.format("Match [%.0f%%]: Ticket #%d ↔ Ticket #%d  (keywords: %s)",
                score * 100, ticketA.getId(), ticketB.getId(), commonKeywords);
        }
    }

    /**
     * Finds potential duplicate/related tickets from a list.
     * Only compares OPEN and IN_PROGRESS tickets.
     *
     * @param tickets all tickets to compare
     * @return list of matches sorted by score (highest first)
     */
    public List<TicketMatch> findRelatedTickets(List<Ticket> tickets) {
        // Filter to active tickets only
        List<Ticket> active = tickets.stream()
            .filter(t -> t.getStatus() == Ticket.Status.OPEN || t.getStatus() == Ticket.Status.IN_PROGRESS)
            .collect(Collectors.toList());

        List<TicketMatch> matches = new ArrayList<>();

        for (int i = 0; i < active.size(); i++) {
            Ticket a = active.get(i);
            Set<String> keywordsA = extractKeywords(a);

            for (int j = i + 1; j < active.size(); j++) {
                Ticket b = active.get(j);
                Set<String> keywordsB = extractKeywords(b);

                // Compute intersection
                Set<String> intersection = new HashSet<>(keywordsA);
                intersection.retainAll(keywordsB);

                if (intersection.size() >= 2) { // require at least 2 common keywords
                    // Jaccard similarity: |A∩B| / |A∪B|
                    Set<String> union = new HashSet<>(keywordsA);
                    union.addAll(keywordsB);
                    double score = (double) intersection.size() / union.size();

                    matches.add(new TicketMatch(a, b, score, intersection));
                }
            }
        }

        // Sort by score descending
        matches.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));
        return matches;
    }

    /**
     * Finds tickets related to a specific ticket.
     *
     * @param target the ticket to find relatives for
     * @param allTickets all tickets pool
     * @return list of matches involving the target ticket
     */
    public List<TicketMatch> findRelatedTo(Ticket target, List<Ticket> allTickets) {
        Set<String> targetKeywords = extractKeywords(target);
        List<TicketMatch> matches = new ArrayList<>();

        for (Ticket other : allTickets) {
            if (other.getId() == target.getId()) continue;
            if (other.getStatus() == Ticket.Status.CLOSED) continue;

            Set<String> otherKeywords = extractKeywords(other);
            Set<String> intersection = new HashSet<>(targetKeywords);
            intersection.retainAll(otherKeywords);

            if (intersection.size() >= 2) {
                Set<String> union = new HashSet<>(targetKeywords);
                union.addAll(otherKeywords);
                double score = (double) intersection.size() / union.size();
                matches.add(new TicketMatch(target, other, score, intersection));
            }
        }

        matches.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));
        return matches;
    }

    /**
     * Extracts meaningful keywords from a ticket's title, description, and category.
     */
    private Set<String> extractKeywords(Ticket ticket) {
        StringBuilder sb = new StringBuilder();
        if (ticket.getComplaintTitle() != null) sb.append(ticket.getComplaintTitle()).append(" ");
        if (ticket.getComplaintDescription() != null) sb.append(ticket.getComplaintDescription()).append(" ");
        if (ticket.getComplaintCategory() != null) sb.append(ticket.getComplaintCategory());

        String text = sb.toString();
        if (text.isBlank()) return Collections.emptySet();

        return Arrays.stream(text.toLowerCase().split("\\W+"))
            .filter(word -> word.length() > 2)
            .filter(word -> !STOP_WORDS.contains(word))
            .collect(Collectors.toSet());
    }
}
