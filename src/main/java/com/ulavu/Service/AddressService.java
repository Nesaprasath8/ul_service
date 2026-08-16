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

import com.ulavu.Entity.UL_Address;

@Service
public class AddressService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Every method takes `username` as resolved server-side from the caller's
    // JWT (see AddressController) - never a client-supplied owner id. The
    // stored procedure itself also re-checks ownership on UPDATE/DELETE as a
    // second layer of defense.

    public List<UL_Address> getAddresses(String username) throws SQLException {
        return jdbcTemplate.execute("{ call ul_sp_addressmanaging(?, ?, ?) }", new CallableStatementCallback<List<UL_Address>>() {
            @Override
            public List<UL_Address> doInCallableStatement(CallableStatement stmt) throws SQLException {
                List<UL_Address> addresses = new ArrayList<>();
                stmt.setString("p_mode", "GET");
                stmt.setNull("p_ad_id", java.sql.Types.INTEGER);
                stmt.setString("p_username", username);
                boolean hasResult = stmt.execute();
                if (!hasResult) {
                    return addresses;
                }
                try (ResultSet rs = stmt.getResultSet()) {
                    while (rs.next()) {
                        addresses.add(mapAddress(rs));
                    }
                }
                return addresses;
            }
        });
    }

    private UL_Address mapAddress(ResultSet rs) throws SQLException {
        UL_Address item = new UL_Address();
        item.id = rs.getInt("id");
        item.addressLine1 = rs.getString("addressline1");
        item.addressLine2 = rs.getString("addressline2");
        item.city = rs.getString("city");
        item.state = rs.getString("state");
        item.postalcode = rs.getString("postalcode");
        item.country = rs.getString("country");
        item.addType = rs.getString("addtype");
        item.isDefault = rs.getString("isdefault");
        return item;
    }

    public String createAddress(String username, UL_Address address) throws SQLException {
        return jdbcTemplate.execute(
                "{ call ul_sp_addressmanaging(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) }",
                (CallableStatementCallback<String>) stmt -> {
                    stmt.setString("p_mode", "INSERT");
                    stmt.setNull("p_ad_id", java.sql.Types.INTEGER);
                    stmt.setString("p_username", username);
                    stmt.setString("p_address_line1", address.addressLine1);
                    stmt.setString("p_address_line2", address.addressLine2);
                    stmt.setString("p_city", address.city);
                    stmt.setString("p_state", address.state);
                    stmt.setString("p_postalcode", address.postalcode);
                    stmt.setString("p_country", address.country);
                    stmt.setString("p_add_type", address.addType);
                    stmt.setString("p_is_default", address.isDefault);
                    stmt.setString("p_actor_id", username);
                    stmt.execute();
                    return "Success";
                });
    }

    public String updateAddress(String username, UL_Address address) throws SQLException {
        return jdbcTemplate.execute(
                "{ call ul_sp_addressmanaging(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) }",
                (CallableStatementCallback<String>) stmt -> {
                    stmt.setString("p_mode", "UPDATE");
                    stmt.setInt("p_ad_id", address.id);
                    stmt.setString("p_username", username);
                    stmt.setString("p_address_line1", address.addressLine1);
                    stmt.setString("p_address_line2", address.addressLine2);
                    stmt.setString("p_city", address.city);
                    stmt.setString("p_state", address.state);
                    stmt.setString("p_postalcode", address.postalcode);
                    stmt.setString("p_country", address.country);
                    stmt.setString("p_add_type", address.addType);
                    stmt.setString("p_is_default", address.isDefault);
                    stmt.setString("p_actor_id", username);
                    stmt.execute();
                    return "Success";
                });
    }

    public String deleteAddress(String username, int addressId) throws SQLException {
        return jdbcTemplate.execute(
                "{ call ul_sp_addressmanaging(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) }",
                (CallableStatementCallback<String>) stmt -> {
                    stmt.setString("p_mode", "DELETE");
                    stmt.setInt("p_ad_id", addressId);
                    stmt.setString("p_username", username);
                    stmt.setNull("p_address_line1", java.sql.Types.VARCHAR);
                    stmt.setNull("p_address_line2", java.sql.Types.VARCHAR);
                    stmt.setNull("p_city", java.sql.Types.VARCHAR);
                    stmt.setNull("p_state", java.sql.Types.VARCHAR);
                    stmt.setNull("p_postalcode", java.sql.Types.VARCHAR);
                    stmt.setNull("p_country", java.sql.Types.VARCHAR);
                    stmt.setNull("p_add_type", java.sql.Types.VARCHAR);
                    stmt.setNull("p_is_default", java.sql.Types.VARCHAR);
                    stmt.setString("p_actor_id", username);
                    stmt.execute();
                    return "Success";
                });
    }
}
