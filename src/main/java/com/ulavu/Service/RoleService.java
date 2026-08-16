package com.ulavu.Service;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.ulavu.Entity.UL_Role;

@Service
public class RoleService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<UL_Role> getRoles() throws SQLException {
        return jdbcTemplate.execute("{ call ul_sp_rolemanaging(?) }", new CallableStatementCallback<List<UL_Role>>() {
            @Override
            public List<UL_Role> doInCallableStatement(CallableStatement stmt) throws SQLException {
                List<UL_Role> roles = new ArrayList<>();
                stmt.setString("p_mode", "GET");
                boolean hasResult = stmt.execute();
                if (!hasResult) {
                    return roles;
                }
                try (ResultSet rs = stmt.getResultSet()) {
                    while (rs.next()) {
                        UL_Role item = new UL_Role();
                        item.id = rs.getInt("id");
                        item.name = rs.getString("name");
                        item.status = rs.getString("status");
                        roles.add(item);
                    }
                }
                return roles;
            }
        });
    }

    public String createRole(UL_Role role) throws SQLException {
        return jdbcTemplate.execute("{ call ul_sp_rolemanaging(?, ?, ?, ?, ?) }", new CallableStatementCallback<String>() {
            @Override
            public String doInCallableStatement(CallableStatement stmt) throws SQLException {
                stmt.setString("p_mode", "INSERT");
                stmt.setNull("p_role_id", java.sql.Types.INTEGER);
                stmt.setString("p_role_name", role.name);
                stmt.setNull("p_status", java.sql.Types.VARCHAR);
                stmt.setString("p_actor_id", role.lst_modifiedby);
                stmt.execute();
                return "Success";
            }
        });
    }

    public String updateRole(UL_Role role) throws SQLException {
        return jdbcTemplate.execute("{ call ul_sp_rolemanaging(?, ?, ?, ?, ?) }", new CallableStatementCallback<String>() {
            @Override
            public String doInCallableStatement(CallableStatement stmt) throws SQLException {
                stmt.setString("p_mode", "UPDATE");
                stmt.setInt("p_role_id", role.id);
                stmt.setString("p_role_name", role.name);
                stmt.setString("p_status", role.status);
                stmt.setString("p_actor_id", role.lst_modifiedby);
                stmt.execute();
                return "Success";
            }
        });
    }

    public String deleteRole(int id, String actorId) throws SQLException {
        return jdbcTemplate.execute("{ call ul_sp_rolemanaging(?, ?, ?, ?, ?) }", new CallableStatementCallback<String>() {
            @Override
            public String doInCallableStatement(CallableStatement stmt) throws SQLException {
                stmt.setString("p_mode", "DELETE");
                stmt.setInt("p_role_id", id);
                stmt.setNull("p_role_name", java.sql.Types.VARCHAR);
                stmt.setNull("p_status", java.sql.Types.VARCHAR);
                stmt.setString("p_actor_id", actorId);
                stmt.execute();
                return "Success";
            }
        });
    }
}
