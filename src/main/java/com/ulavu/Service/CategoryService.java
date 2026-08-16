package com.ulavu.Service;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.ulavu.Entity.UL_Category;

@Service
public class CategoryService {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private UL_Category mapCategory(ResultSet rs) throws SQLException {
        UL_Category item = new UL_Category();
        item.id = rs.getInt("id");
        item.name = rs.getString("name");
        item.description = rs.getString("description");
        item.slug = rs.getString("slug");
        item.par_id = (Integer) rs.getObject("parent_id");
        return item;
    }

    public List<UL_Category> getCategories() throws SQLException{
        return jdbcTemplate.execute("{ call ul_sp_categorymanaging(?) }", new CallableStatementCallback<List<UL_Category>>() {
            @Override
            public List<UL_Category> doInCallableStatement(CallableStatement stmt) throws SQLException {
                List<UL_Category> categories = new ArrayList<>();
                stmt.setString("p_mode", "GET");
                boolean hasResult = stmt.execute();
                if (!hasResult) {
                    return categories;
                }
                try (ResultSet rs = stmt.getResultSet()) {
                    while (rs.next()) {
                        categories.add(mapCategory(rs));
                    }
                }
                return categories;
            }
        });
    }

    public UL_Category getCategoryById(int id) throws SQLException {
        return jdbcTemplate.execute("{ call ul_sp_categorymanaging(?, ?) }", new CallableStatementCallback<UL_Category>() {
            @Override
            public UL_Category doInCallableStatement(CallableStatement stmt) throws SQLException {
                stmt.setString("p_mode", "GET");
                stmt.setInt("p_cat_id", id);
                boolean hasResult = stmt.execute();
                if (!hasResult) {
                    return null;
                }
                try (ResultSet rs = stmt.getResultSet()) {
                    if (rs.next()) {
                        return mapCategory(rs);
                    }
                }
                return null;
            }
        });
    }

    public String insertCategory(UL_Category category) throws SQLException {
        return jdbcTemplate.execute("{ call ul_sp_categorymanaging(?, ?, ?, ?, ?, ?) }", new CallableStatementCallback<String>() {
            @Override
            public String doInCallableStatement(CallableStatement stmt) throws SQLException {
                stmt.setString("p_mode", "INSERT");
                stmt.setString("p_cat_name", category.name);
                stmt.setString("p_cat_description", category.description);
                stmt.setString("p_cat_slug", category.slug);
                // BUGFIX: these two were previously bound under the wrong
                // parameter names (p_cat_par_id, p_lst_modifiedby), neither
                // of which exists on the procedure (it's p_cat_parid and
                // p_actor_id) - every category creation call would have
                // failed with a "parameter does not exist" SQL error.
                if (category.par_id != null) {
                    stmt.setInt("p_cat_parid", category.par_id);
                } else {
                    stmt.setNull("p_cat_parid", Types.INTEGER);
                }
                stmt.setString("p_actor_id", category.lst_modifiedby);
                stmt.execute();
                return "Success";
            }
        });
    }

    public String updateCategory(UL_Category category) throws SQLException {
        return jdbcTemplate.execute("{ call ul_sp_categorymanaging(?, ?, ?, ?, ?, ?, ?) }", new CallableStatementCallback<String>() {
            @Override
            public String doInCallableStatement(CallableStatement stmt) throws SQLException {
                stmt.setString("p_mode", "UPDATE");
                stmt.setInt("p_cat_id", category.id);
                stmt.setString("p_cat_name", category.name);
                stmt.setString("p_cat_slug", category.slug);
                stmt.setString("p_cat_description", category.description);
                if (category.par_id != null) {
                    stmt.setInt("p_cat_parid", category.par_id);
                } else {
                    stmt.setNull("p_cat_parid", Types.INTEGER);
                }
                stmt.setString("p_actor_id", category.lst_modifiedby);
                stmt.execute();
                return "Success";
            }
        });
    }

    public String deleteCategory(int id, String actorId) throws SQLException {
        return jdbcTemplate.execute("{ call ul_sp_categorymanaging(?, ?, ?, ?, ?, ?, ?) }", new CallableStatementCallback<String>() {
            @Override
            public String doInCallableStatement(CallableStatement stmt) throws SQLException {
                stmt.setString("p_mode", "DELETE");
                stmt.setInt("p_cat_id", id);
                stmt.setNull("p_cat_name", Types.VARCHAR);
                stmt.setNull("p_cat_slug", Types.VARCHAR);
                stmt.setNull("p_cat_description", Types.VARCHAR);
                stmt.setNull("p_cat_parid", Types.INTEGER);
                stmt.setString("p_actor_id", actorId);
                stmt.execute();
                return "Success";
            }
        });
    }
}
