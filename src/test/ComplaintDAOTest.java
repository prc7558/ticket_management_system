package test;

import dao.ComplaintDAO;
import model.Complaint;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit tests for ComplaintDAO.
 * Requires seeded user with id=2 (Test User from schema.sql).
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ComplaintDAOTest {

    private static ComplaintDAO dao;
    private static int testComplaintId;

    @BeforeAll
    static void setup() {
        dao = new ComplaintDAO();
    }

    @Test
    @Order(1)
    void testInsertComplaint() {
        Complaint c = new Complaint(2, "JUnit Test Complaint", "This is a test description", "Technical");
        int id = dao.insertComplaint(c);
        assertTrue(id > 0, "Complaint insert should return positive ID");
        testComplaintId = id;
    }

    @Test
    @Order(2)
    void testFindById() {
        Complaint c = dao.findById(testComplaintId);
        assertNotNull(c, "Should find complaint by ID");
        assertEquals("JUnit Test Complaint", c.getTitle());
        assertEquals("Technical", c.getCategory());
    }

    @Test
    @Order(3)
    void testFindByUserId() {
        var list = dao.findByUserId(2);
        assertNotNull(list);
        assertTrue(list.size() >= 1, "User should have at least one complaint");
    }

    @Test
    @Order(4)
    void testUpdateComplaint() {
        Complaint c = dao.findById(testComplaintId);
        assertNotNull(c);
        c.setTitle("Updated JUnit Complaint");
        c.setCategory("Billing");
        assertTrue(dao.updateComplaint(c), "Update should succeed");

        Complaint updated = dao.findById(testComplaintId);
        assertEquals("Updated JUnit Complaint", updated.getTitle());
        assertEquals("Billing", updated.getCategory());
    }

    @Test
    @Order(5)
    void testCountAll() {
        int count = dao.countAll();
        assertTrue(count >= 1, "Should have at least one complaint");
    }

    @Test
    @Order(6)
    void testDeleteComplaint() {
        assertTrue(dao.deleteComplaint(testComplaintId), "Delete should succeed");
        assertNull(dao.findById(testComplaintId), "Deleted complaint should not be found");
    }
}
