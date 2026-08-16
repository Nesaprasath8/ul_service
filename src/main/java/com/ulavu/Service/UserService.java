package com.ulavu.Service;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.ulavu.Entity.UL_UserEntity;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final DataSource dataSource;

    public UserService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // Uses the Spring Boot-managed (pooled) DataSource instead of opening a raw
    // DriverManager connection per call - avoids unbounded connection creation
    // under load and picks up standard pool timeouts/limits.
    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public String createUser(UL_UserEntity userdetail) throws SQLException {
        StringBuilder sql = new StringBuilder("CALL ul_sp_usermanaging(");
        List<Object> valuesToBind = new ArrayList<>();
        sql.append("p_mode => ?, ");
        sql.append("p_username => ?, ");
        sql.append("p_password => ?, ");
        sql.append("p_first_name => ?, ");
        sql.append("p_last_name => ?, ");
        sql.append("p_email_id => ?, ");
        sql.append("p_mobile_no => ?, ");
        sql.append("p_role_id => ?::integer, ");
        sql.append("p_status => ?, ");
        sql.append("p_actor_id => ?");
        sql.append(")");
        valuesToBind.add("INSERT");
        valuesToBind.add(userdetail.username);
        valuesToBind.add(userdetail.password);
        valuesToBind.add(userdetail.firstname);
        valuesToBind.add(userdetail.lastname);
        valuesToBind.add(userdetail.emailId);
        valuesToBind.add(userdetail.mobileno);
        valuesToBind.add(userdetail.userrole);
        valuesToBind.add("l");
        valuesToBind.add(userdetail.lst_modifiedby);

        try (Connection conn = getConnection();
                CallableStatement stmt = conn.prepareCall(sql.toString())) {
            IntStream.range(0, valuesToBind.size()).forEach(i -> {
                try {
                    stmt.setObject(i + 1, valuesToBind.get(i));
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
            stmt.execute();
        }

        return "Success";
    }

    public String updateUser(UL_UserEntity userdetail) throws SQLException {
        StringBuilder sql = new StringBuilder("CALL ul_sp_usermanaging(");
        List<Object> valuesToBind = new ArrayList<>();
        sql.append("p_mode => ?, ");
        sql.append("p_username => ?, ");
        sql.append("p_password => ?, ");
        sql.append("p_first_name => ?, ");
        sql.append("p_last_name => ?, ");
        sql.append("p_email_id => ?, ");
        sql.append("p_mobile_no => ?, ");
        sql.append("p_role_id => ?::integer, ");
        sql.append("p_status => ?, ");
        sql.append("p_actor_id => ?");
        sql.append(")");
        valuesToBind.add("UPDATE");
        valuesToBind.add(userdetail.username);
        valuesToBind.add(userdetail.password);
        valuesToBind.add(userdetail.firstname);
        valuesToBind.add(userdetail.lastname);
        valuesToBind.add(userdetail.emailId);
        valuesToBind.add(userdetail.mobileno);
        valuesToBind.add(userdetail.userrole);
        valuesToBind.add("l");
        valuesToBind.add(userdetail.lst_modifiedby);

        try (Connection conn = getConnection();
                CallableStatement stmt = conn.prepareCall(sql.toString())) {
            IntStream.range(0, valuesToBind.size()).forEach(i -> {
                try {
                    stmt.setObject(i + 1, valuesToBind.get(i));
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
            stmt.execute();
        }

        return "Success";
    }

    public String deleteUser(UL_UserEntity userdetail) throws SQLException {
        StringBuilder sql = new StringBuilder("CALL ul_sp_usermanaging(");
        List<Object> valuesToBind = new ArrayList<>();
        sql.append("p_mode => ?, ");
        sql.append("p_username => ?, ");
        sql.append("p_actor_id => ?");
        valuesToBind.add("DELETE");
        sql.append(")");
        valuesToBind.add(userdetail.username);
        valuesToBind.add(userdetail.lst_modifiedby);

        try (Connection conn = getConnection();
                CallableStatement stmt = conn.prepareCall(sql.toString())) {
            IntStream.range(0, valuesToBind.size()).forEach(i -> {
                try {
                    stmt.setObject(i + 1, valuesToBind.get(i));
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
            stmt.execute();
        }

        return "Success";
    }

    /**
     * Fetches a user's own profile (no password). username must come from
     * the caller's validated JWT (see UserProfileController) - never from a
     * client-supplied value - so one user can't fetch another user's profile
     * simply by naming them.
     */
    public UL_UserEntity getUserProfile(String username) throws SQLException {
        StringBuilder sql = new StringBuilder("CALL ul_sp_usermanaging(");
        sql.append("p_mode => ?, ");
        sql.append("p_username => ?, ");
        sql.append("p_usrdet_cursor => ?");
        sql.append(")");

        try (Connection conn = getConnection();
                CallableStatement stmt = conn.prepareCall(sql.toString())) {
            stmt.setString(1, "GET");
            stmt.setString(2, username);
            stmt.registerOutParameter(3, Types.REF_CURSOR);
            stmt.execute();
            try (ResultSet rs = (ResultSet) stmt.getObject(3)) {
                if (rs.next()) {
                    UL_UserEntity found = new UL_UserEntity();
                    found.username = rs.getString("username");
                    found.firstname = rs.getString("firstname");
                    found.lastname = rs.getString("lastname");
                    found.emailId = rs.getString("emailid");
                    found.mobileno = rs.getString("mobileno");
                    found.userrole = (Integer) rs.getObject("roleid");
                    found.status = rs.getString("status");
                    return found;
                }
            }
        }
        return null;
    }

    public UL_UserEntity checkLogin(UL_UserEntity userdetail) throws  SQLException{
        StringBuilder sql = new StringBuilder("CALL ul_sp_usermanaging(");
        sql.append("p_mode => ?, ");
        sql.append("p_username => ?, ");
        sql.append("p_password => ?, ");
        sql.append("p_usrdet_cursor => ?");
        sql.append(")");

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false); 

            try (CallableStatement stmt = conn.prepareCall(sql.toString())) {
                stmt.setString(1, "LOGIN");
                stmt.setString(2, userdetail.username);
                stmt.setString(3, userdetail.password);
                stmt.registerOutParameter(4, Types.REF_CURSOR);
                stmt.execute();
                try (ResultSet rs = (ResultSet) stmt.getObject(4)) {
                    UL_UserEntity found = null;
                    if (rs.next()) {
                        found = new UL_UserEntity();
                        try {
                            found.username = rs.getString("username");
                        } catch (SQLException ex) {
                            found.username = userdetail.username;
                        }
                        try { found.emailId = rs.getString("email_id"); } catch (SQLException ex) { found.emailId = rs.getString("emailId"); }
                        try { found.userId = rs.getInt("user_id"); } catch (SQLException ex) { /* ignore */ }
                        try { found.userrole = rs.getInt("role_id"); } catch (SQLException ex) { try { found.userrole = rs.getInt("userrole"); } catch (SQLException ex2) { /* ignore */ } }
                    }
                    conn.commit(); 
                    return found;
                }
            } catch (SQLException e) {
                conn.rollback(); // Rollback transaction if database execution fails
                throw e;
            }
        } catch (SQLException e) {
            log.error("Login query failed for user '{}'", userdetail.username, e);
            throw e;
        }
    }
}
