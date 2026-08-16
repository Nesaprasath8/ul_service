package com.ulavu.Service;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.stereotype.Service;

import com.ulavu.Entity.UL_Product;

@Service
public class ProductService {

    private final DataSource dataSource;

    public ProductService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // Uses the Spring Boot-managed (pooled) DataSource instead of opening a raw
    // DriverManager connection per call - avoids unbounded connection creation
    // under load and picks up standard pool timeouts/limits.
    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    private void bindParameters(CallableStatement stmt, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            stmt.setObject(i + 1, params[i]);
        }
    }

    // BUGFIX: this previously tried rs.getObject("p_prd_id") / getString("p_prd_name")
    // etc. first and only fell back to the plain column name (e.g. "id") if that
    // returned null. But rs.getObject/getString THROWS when the column doesn't
    // exist at all, rather than returning null - and the GET query has never
    // returned any "p_prd_*"-prefixed columns, only the plain aliases below. So
    // every single call to this method was throwing on its very first line,
    // meaning getProducts()/getProductById() always failed. Rewritten to read
    // exactly the columns the GET query in Product_Management_SP.sql actually
    // returns.
    private UL_Product mapProduct(ResultSet rs) throws SQLException {
        UL_Product item = new UL_Product();
        item.id = (Integer) rs.getObject("id");
        item.name = rs.getString("name");
        item.description = rs.getString("description");
        item.slug = rs.getString("slug");
        item.quantity = (Integer) rs.getObject("quantity");
        item.price = rs.getString("price");
        item.comparePrice = rs.getString("compareprice");
        item.categoryId = (Integer) rs.getObject("category_id");
        item.categoryName = rs.getString("categoryname");
        return item;
    }

    public List<UL_Product> getProducts(UL_Product product, String param) throws SQLException {
        StringBuilder sql = new StringBuilder("CALL ul_sp_productmanaging(");
        List<Object> valuesToBind = new ArrayList<>();
        sql.append("p_mode => ?");
        sql.append(", p_prd_name => ?");
        sql.append(", p_param => ?");
        sql.append(", p_prddet_cursor => ?");
        sql.append(")");
        valuesToBind.add("GET");
        valuesToBind.add(product != null ? product.name : null);
        valuesToBind.add(param);

        try (Connection conn = getConnection();
                CallableStatement stmt = conn.prepareCall(sql.toString())) {
            bindParameters(stmt, valuesToBind.toArray(new Object[0]));
            stmt.registerOutParameter(valuesToBind.size(), Types.REF_CURSOR);
            List<UL_Product> products = new ArrayList<>();
            try (ResultSet rs = (ResultSet) stmt.getObject(valuesToBind.size())) {
                while (rs.next()) {
                    products.add(mapProduct(rs));
                }
            }
            return products;
        }
    }

    public UL_Product getProductById(int id) throws SQLException {
        StringBuilder sql = new StringBuilder("CALL ul_sp_productmanaging(");
        List<Object> valuesToBind = new ArrayList<>();
        sql.append("p_mode => ?");
        sql.append(", p_prd_id => ?");
        sql.append(", p_param => ?");
        sql.append(", p_prddet_cursor => ?");
        sql.append(")");
        valuesToBind.add("GET");
        valuesToBind.add(id);
        valuesToBind.add("id");

        try (Connection conn = getConnection();
                CallableStatement stmt = conn.prepareCall(sql.toString())) {
            bindParameters(stmt, valuesToBind.toArray(new Object[0]));
            stmt.registerOutParameter(valuesToBind.size(), Types.REF_CURSOR);

            try (ResultSet rs = (ResultSet) stmt.getObject(valuesToBind.size())) {
                if (rs.next()) {
                    return mapProduct(rs);
                }
            }
        }

        return null;
    }

    public String createProduct(UL_Product product) throws SQLException {
        StringBuilder sql = new StringBuilder("CALL ul_sp_productmanaging(");
        List<Object> valuesToBind = new ArrayList<>();
        sql.append("p_mode => ?");
        sql.append(", p_prd_name => ?");
        sql.append(", p_prd_description => ?");
        // BUGFIX: every one of the four parameter names below was wrong
        // and did not exist on the procedure - p_prd_price should be
        // p_prd_base_price, p_prd_category should be p_prd_category_id
        // (and needs an integer, not the free-text string the old
        // UL_Product.category field held), p_prd_quantity should be
        // p_prd_stock_quantity, and p_lst_modifiedby should be
        // p_actor_id. Product creation has never worked because of this.
        sql.append(", p_prd_base_price => ?");
        sql.append(", p_prd_category_id => ?");
        sql.append(", p_prd_slug => ?");
        sql.append(", p_prd_compare_price => ?");
        sql.append(", p_prd_stock_quantity => ?");
        sql.append(", p_actor_id => ?");
        sql.append(")");
        valuesToBind.add("INSERT");
        valuesToBind.add(product != null ? product.name : null);
        valuesToBind.add(product != null ? product.description : null);
        valuesToBind.add(product != null ? product.price : null);
        valuesToBind.add(product != null ? product.categoryId : null);
        valuesToBind.add(product != null ? product.slug : null);
        valuesToBind.add(product != null ? product.comparePrice : null);
        valuesToBind.add(product != null ? product.quantity : null);
        valuesToBind.add(product != null ? product.lst_modifiedby : null);

        try (Connection conn = getConnection();
                CallableStatement stmt = conn.prepareCall(sql.toString())) {
            bindParameters(stmt, valuesToBind.toArray(new Object[0]));
            stmt.execute();
        }

        return "Success";
    }

    public String updateProduct(UL_Product product) throws SQLException {
        StringBuilder sql = new StringBuilder("CALL ul_sp_productmanaging(");
        List<Object> valuesToBind = new ArrayList<>();
        sql.append("p_mode => ?");
        sql.append(", p_prd_id => ?");
        sql.append(", p_prd_name => ?");
        sql.append(", p_prd_description => ?");
        sql.append(", p_prd_base_price => ?");
        sql.append(", p_prd_category_id => ?");
        sql.append(", p_prd_slug => ?");
        sql.append(", p_prd_compare_price => ?");
        sql.append(", p_prd_stock_quantity => ?");
        sql.append(", p_actor_id => ?");
        sql.append(")");
        valuesToBind.add("UPDATE");
        valuesToBind.add(product != null ? product.id : null);
        valuesToBind.add(product != null ? product.name : null);
        valuesToBind.add(product != null ? product.description : null);
        valuesToBind.add(product != null ? product.price : null);
        valuesToBind.add(product != null ? product.categoryId : null);
        valuesToBind.add(product != null ? product.slug : null);
        valuesToBind.add(product != null ? product.comparePrice : null);
        valuesToBind.add(product != null ? product.quantity : null);
        valuesToBind.add(product != null ? product.lst_modifiedby : null);

        try (Connection conn = getConnection();
                CallableStatement stmt = conn.prepareCall(sql.toString())) {
            bindParameters(stmt, valuesToBind.toArray(new Object[0]));
            stmt.execute();
        }

        return "Success";
    }

    public String deleteProduct(int id, String actorId) throws SQLException {
        StringBuilder sql = new StringBuilder("CALL ul_sp_productmanaging(");
        List<Object> valuesToBind = new ArrayList<>();
        sql.append("p_mode => ?");
        sql.append(", p_prd_id => ?");
        sql.append(", p_actor_id => ?");
        sql.append(")");
        valuesToBind.add("DELETE");
        valuesToBind.add(id);
        valuesToBind.add(actorId);

        try (Connection conn = getConnection();
                CallableStatement stmt = conn.prepareCall(sql.toString())) {
            bindParameters(stmt, valuesToBind.toArray(new Object[0]));
            stmt.execute();
        }

        return "Success";
    }
}
