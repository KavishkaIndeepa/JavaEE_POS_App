package jsp.servlet;

import javax.json.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

import static java.lang.Class.forName;

@WebServlet(urlPatterns = {"/pages/purchaseOrder"})
public class PurchaseOrderServletAPI extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String option = req.getParameter("option");


        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/asestra", "root", "1234");

            switch (option) {
                case "customer":
                    String id1=req.getParameter("id");
                    PreparedStatement pstm = connection.prepareStatement("select * from customer where id=?");

                    pstm.setString(1,id1);

                    ResultSet rst = pstm.executeQuery();
                    resp.addHeader("Content-Type","application/json");
                    resp.addHeader("Access-Control-Allow-Origin","*");


                    JsonObjectBuilder customerBuilder = Json.createObjectBuilder();

                    while (rst.next()) {

                        String id = rst.getString(1);
                        String name = rst.getString(2);
                        String address = rst.getString(3);


                        customerBuilder.add("id",id);
                        customerBuilder.add("name",name);
                        customerBuilder.add("address",address);

                    }

                    resp.getWriter().print(customerBuilder.build());

                    break;
                case "items":
                    String code1=req.getParameter("code");
                    PreparedStatement pstm1 = connection.prepareStatement("select * from item where code=?");

                    pstm1.setString(1,code1);
                    ResultSet rst1 = pstm1.executeQuery();

                    resp.addHeader("Content-Type","application/json");
                    resp.addHeader("Access-Control-Allow-Origin","*");

//                    JsonArrayBuilder allItem = Json.createArrayBuilder();

                    JsonObjectBuilder itemBuilder= Json.createObjectBuilder();
                    while (rst1.next()) {

                        String code = rst1.getString(1);
                        String name = rst1.getString(2);
                        int qtyOnHand = rst1.getInt(3);
                        double unitPrice = rst1.getDouble(4);


                        itemBuilder.add("code",code);
                        itemBuilder.add("description",name);
                        itemBuilder.add("qtyOnHand",qtyOnHand);
                        itemBuilder.add("unitPrice",unitPrice);

                    }

                    resp.getWriter().print(itemBuilder.build());
                    break;
            }



        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        resp.addHeader("Access-Control-Allow-Origin","*");
        resp.addHeader("Content-Type","application/json");


//        String itemCode = jsonObject.getString("itemCode");
//        String qty = jsonObject.getString("qty");
//        String unitPrice = jsonObject.getString("unitPrice");
//        JsonArray orderDetails = jsonObject.getJsonArray("orderDetails");

        try {
            forName("com.mysql.jdbc.Driver");
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/asestra", "root", "1234");
            connection.setAutoCommit(false);

            JsonReader reader = Json.createReader(req.getReader());
            JsonObject jsonObject = reader.readObject();

            String orderId = jsonObject.getString("oid");
            String orderDate = jsonObject.getString("date");
            String customerId = jsonObject.getString("odCusId");
            System.out.println(orderId+orderDate+customerId);

            PreparedStatement orderStatement = connection.prepareStatement("INSERT INTO orders VALUES(?,?,?)");
            orderStatement.setString(1, orderId);
            orderStatement.setString(2, orderDate);
            orderStatement.setString(3, customerId);

//            int affectedRows = orderStatement.executeUpdate();
//            if (affectedRows == 0) {
//                connection.rollback();
//                throw new RuntimeException("Failed to save the order");
//            } else {
//                System.out.println("Order Saved");
//
//            }
            if (orderStatement.executeUpdate() > 0) {
                connection.rollback();
                connection.setAutoCommit(true);
                throw new SQLException("Order Not Added");

            }

            JsonArray odDetail = jsonObject.getJsonArray("odDetail");
            for (JsonValue orderDetail: odDetail) {
                JsonObject odObject = orderDetail.asJsonObject();
                String itemCode = odObject.getString("code");
                String qty = odObject.getString("qty");
                String byQty = odObject.getString("byQty");
                String unitPrice = odObject.getString("price");

                PreparedStatement pstm2 = connection.prepareStatement("insert into orderdetails values(?,?,?,?)");
                pstm2.setObject(1, orderId);
                pstm2.setObject(2, itemCode);
                pstm2.setObject(3,byQty );
                pstm2.setObject(4, unitPrice);

                System.out.println(itemCode+qty);

                if (!(pstm2.executeUpdate() > 0)) {
                    connection.rollback();
                    connection.setAutoCommit(true);
                    throw new SQLException("Order Details Not added.!");
                }


                PreparedStatement pstm3 = connection.prepareStatement("update item set qty=? where code=?");
                pstm3.setObject(2, itemCode);
                int bvQty=Integer.parseInt(byQty);
                int avQty=Integer.parseInt(qty);
                pstm3.setInt(1,(avQty-bvQty));
                if (!(pstm3.executeUpdate() > 0)) {
                    connection.rollback();
                    connection.setAutoCommit(true);
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    resp.getWriter().println(Json.createObjectBuilder()
                            .add("Status", "Error")
                            .add("message", "Order Details Not updated")
                            .add("data", "")
                            .build()
                            .toString());
                    return;
                }
            }
            connection.commit();
            connection.setAutoCommit(true);
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().println(Json.createObjectBuilder()
                    .add("Status", "OK")
                    .add("message", "Successfully Added")
                    .add("data", "")
                    .build()
                    .toString());

        } catch (ClassNotFoundException | SQLException e) {
            JsonObjectBuilder error = Json.createObjectBuilder();
            error.add("Status","Error");
            error.add("message",e.getLocalizedMessage());
            error.add("data","");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().print(error.build());

        }
        
        
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.addHeader("Access-Control-Allow-Origin", "*");
        resp.addHeader("Access-Control-Allow-Headers", "Content-Type");
        resp.addHeader("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE");
    }
}
