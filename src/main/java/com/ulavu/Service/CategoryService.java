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

import com.ulavu.Entity.UL_Category;

@Service
public class CategoryService {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<UL_Category> getCategories() throws SQLException{
        // Implement logic to retrieve categories from the database
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
                        UL_Category item = new UL_Category();
                            item.id = rs.getInt("id");
                        item.name = rs.getString("name");
                        item.description = rs.getString("description");
                        item.slug = rs.getString("slug");
                        item.par_id = rs.getInt("parent_id");
                        categories.add(item);
                    }
                }
                return categories;
            }
        });
    }
    
    public String insertCategory(UL_Category category) throws SQLException {
        // Implement logic to insert a new category into the database
        return jdbcTemplate.execute("{ call ul_sp_categorymanaging(?, ?, ?, ?, ?, ?) }", new CallableStatementCallback<String>() {
            @Override
            public String doInCallableStatement(CallableStatement stmt) throws SQLException {
                stmt.setString("p_mode", "INSERT");
                stmt.setString("p_cat_name", category.name);
                stmt.setString("p_cat_description", category.description);
                stmt.setString("p_cat_slug", category.slug);
                stmt.setInt("p_cat_par_id", category.par_id != null ? category.par_id : 0);
                stmt.setString("p_lst_modifiedby", category.lst_modifiedby);
                stmt.execute();
                return "Success";
            }
        });
    }
}
