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

    private UL_Product mapProduct(ResultSet rs) throws SQLException {
        UL_Product item = new UL_Product();
        item.id = (Integer) rs.getObject("p_prd_id");
        if (item.id == null) {
            item.id = (Integer) rs.getObject("id");
        }

        item.name = rs.getString("p_prd_name");
        if (item.name == null) {
            item.name = rs.getString("name");
        }

        item.description = rs.getString("p_prd_description");
        if (item.description == null) {
            item.description = rs.getString("description");
        }

        item.price = rs.getString("p_prd_price");
        if (item.price == null) {
            item.price = rs.getString("price");
        }

        item.category = rs.getString("p_prd_category");
        if (item.category == null) {
            item.category = rs.getString("category");
        }

        item.slug = rs.getString("p_prd_slug");
        item.comparePrice = rs.getString("p_prd_compare_price");

        item.quantity = (Integer) rs.getObject("p_prd_quantity");
        if (item.quantity == null) {
            item.quantity = (Integer) rs.getObject("quantity");
        }

        Object statusObj = rs.getObject("p_prd_status");
        if (statusObj != null) {
            String statusValue = statusObj.toString();
            item.status = statusValue.isEmpty() ? null : statusValue.charAt(0);
        }

        item.lst_modifiedby = rs.getString("p_lst_modifiedby");
        return item;
    }

    public List<UL_Product> getProducts(UL_Product product, String param) throws SQLException {
        StringBuilder sql = new StringBuilder("CALL ul_sp_productmanaging(");
        List<Object> valuesToBind = new ArrayList<>();
        sql.append("p_mode => ?");
        sql.append(", p_prd_name => ?");
        sql.append(", p_param => ?");
        // BUGFIX: this was previously missing the separating comma
        // ("p_param => ?" + "p_prddet_cursor => ?" concatenated with no comma),
        // which produced invalid SQL and would fail at execution time.
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
        sql.append(", p_prd_price => ?");
        sql.append(", p_prd_category => ?");
        sql.append(", p_prd_slug => ?");
        sql.append(", p_prd_compare_price => ?");
        sql.append(", p_prd_quantity => ?");
        sql.append(", p_lst_modifiedby => ?");
        sql.append(")");
        valuesToBind.add("INSERT");
        valuesToBind.add(product != null ? product.name : null);
        valuesToBind.add(product != null ? product.description : null);
        valuesToBind.add(product != null ? product.price : null);
        valuesToBind.add(product != null ? product.category : null);
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
        sql.append(", p_prd_price => ?");
        sql.append(", p_prd_category => ?");
        sql.append(", p_prd_slug => ?");
        sql.append(", p_prd_compare_price => ?");
        sql.append(", p_prd_quantity => ?");
        sql.append(", p_lst_modifiedby => ?");
        sql.append(")");
        valuesToBind.add("UPDATE");
        valuesToBind.add(product != null ? product.id : null);
        valuesToBind.add(product != null ? product.name : null);
        valuesToBind.add(product != null ? product.description : null);
        valuesToBind.add(product != null ? product.price : null);
        valuesToBind.add(product != null ? product.category : null);
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

    public String deleteProduct(int id) throws SQLException {
        StringBuilder sql = new StringBuilder("CALL ul_sp_productmanaging(");
        List<Object> valuesToBind = new ArrayList<>();
        sql.append("p_mode => ?");
        sql.append(", p_prd_id => ?");
        sql.append(", p_param => ?");
        sql.append(")");
        valuesToBind.add("DELETE");
        valuesToBind.add(id);
        valuesToBind.add("id");

        try (Connection conn = getConnection();
                CallableStatement stmt = conn.prepareCall(sql.toString())) {
            bindParameters(stmt, valuesToBind.toArray(new Object[0]));
            stmt.execute();
        }

        return "Success";
    }
}
