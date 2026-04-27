package test;

import dao.TicketDAO;
import dao.ComplaintDAO;
import model.Complaint;
import model.Ticket;
import org.junit.jupiter.api.*;

import java.sql.Timestamp;
import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit tests for TicketDAO.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TicketDAOTest {

    private static TicketDAO ticketDAO;
    private static ComplaintDAO complaintDAO;
    private static int testComplaintId;
    private static int testTicketId;

    @BeforeAll
    static void setup() {
        ticketDAO = new TicketDAO();
        complaintDAO = new ComplaintDAO();
        // Create a complaint for ticket tests
        Complaint c = new Complaint(2, "Ticket Test Complaint", "Description for ticket test", "Service");
        testComplaintId = complaintDAO.insertComplaint(c);
        assertTrue(testComplaintId > 0, "Setup: complaint creation must succeed");
    }

    @Test
    @Order(1)
    void testInsertTicket() {
        Ticket t = new Ticket(testComplaintId, "MEDIUM", Ticket.Status.OPEN);
        t.setSlaDeadline(new Timestamp(System.currentTimeMillis() + 48 * 3600 * 1000L));
        int id = ticketDAO.insertTicket(t);
        assertTrue(id > 0, "Ticket insert should return positive ID");
        testTicketId = id;
    }

    @Test
    @Order(2)
    void testFindById() {
        Ticket t = ticketDAO.findById(testTicketId);
        assertNotNull(t, "Should find ticket by ID");
        assertEquals("MEDIUM", t.getPriority());
        assertEquals(Ticket.Status.OPEN, t.getStatus());
    }

    @Test
    @Order(3)
    void testFindByUserId() {
        var list = ticketDAO.findByUserId(2);
        assertNotNull(list);
        assertTrue(list.size() >= 1);
    }

    @Test
    @Order(4)
    void testUpdatePriority() {
        assertTrue(ticketDAO.updatePriority(testTicketId, "HIGH"));
        Ticket t = ticketDAO.findById(testTicketId);
        assertEquals("HIGH", t.getPriority());
    }

    @Test
    @Order(5)
    void testUpdateStatus() {
        assertTrue(ticketDAO.updateStatus(testTicketId, Ticket.Status.IN_PROGRESS));
        Ticket t = ticketDAO.findById(testTicketId);
        assertEquals(Ticket.Status.IN_PROGRESS, t.getStatus());
    }

    @Test
    @Order(6)
    void testAssignAdmin() {
        assertTrue(ticketDAO.assignAdmin(testTicketId, 1)); // admin id=1
        Ticket t = ticketDAO.findById(testTicketId);
        assertEquals(1, t.getAssignedAdminId());
    }

    @Test
    @Order(7)
    void testSearch() {
        var results = ticketDAO.search("Ticket Test");
        assertNotNull(results);
        assertTrue(results.size() >= 1, "Search should find the test ticket");
    }

    @Test
    @Order(8)
    void testCountByStatus() {
        int count = ticketDAO.countByStatus(Ticket.Status.IN_PROGRESS);
        assertTrue(count >= 1);
    }

    @Test
    @Order(9)
    void testCountAll() {
        int count = ticketDAO.countAll();
        assertTrue(count >= 1);
    }

    @AfterAll
    static void cleanup() {
        // Clean up by deleting the test complaint (cascades to ticket)
        complaintDAO.deleteComplaint(testComplaintId);
    }
}
