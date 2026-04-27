package test;

import dao.UserDAO;
import model.User;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit tests for UserDAO.
 * Requires a running MySQL test database.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserDAOTest {

    private static UserDAO dao;
    private static int testUserId;

    @BeforeAll
    static void setup() {
        dao = new UserDAO();
    }

    @Test
    @Order(1)
    void testInsertUser() {
        User user = new User("Test JUnit User", "junit_test@test.com",
                "5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8", // password
                User.Role.USER);
        int id = dao.insertUser(user);
        assertTrue(id > 0, "User insert should return positive ID");
        testUserId = id;
    }

    @Test
    @Order(2)
    void testFindByEmail() {
        User user = dao.findByEmail("junit_test@test.com");
        assertNotNull(user, "Should find user by email");
        assertEquals("Test JUnit User", user.getName());
        assertEquals(User.Role.USER, user.getRole());
    }

    @Test
    @Order(3)
    void testFindById() {
        User user = dao.findById(testUserId);
        assertNotNull(user, "Should find user by ID");
        assertEquals(testUserId, user.getId());
    }

    @Test
    @Order(4)
    void testUpdateUser() {
        User user = dao.findById(testUserId);
        assertNotNull(user);
        user.setName("Updated JUnit User");
        assertTrue(dao.updateUser(user), "Update should succeed");

        User updated = dao.findById(testUserId);
        assertEquals("Updated JUnit User", updated.getName());
    }

    @Test
    @Order(5)
    void testFindAllAdmins() {
        var admins = dao.findAllAdmins();
        assertNotNull(admins);
        assertTrue(admins.size() >= 1, "Should have at least one admin (seeded)");
    }

    @Test
    @Order(6)
    void testCountByRole() {
        int count = dao.countByRole(User.Role.USER);
        assertTrue(count >= 1, "Should have at least one user");
    }

    @Test
    @Order(7)
    void testDuplicateEmail() {
        User dup = new User("Dup", "junit_test@test.com", "hash", User.Role.USER);
        int id = dao.insertUser(dup);
        assertEquals(-1, id, "Duplicate email insert should fail");
    }

    @Test
    @Order(8)
    void testDeleteUser() {
        assertTrue(dao.deleteUser(testUserId), "Delete should succeed");
        assertNull(dao.findById(testUserId), "Deleted user should not be found");
    }
}
