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
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/company", "root", "1234");

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

       /* try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/company", "root", "1234");
            PreparedStatement pstm = connection.prepareStatement("select * from orders");
//            PreparedStatement pstm2 = connection.prepareStatement("select * from order_detail");
            ResultSet rst = pstm.executeQuery();
//            ResultSet rst2 = pstm2.executeQuery();
            PrintWriter writer = resp.getWriter();
            resp.addHeader("Access-Control-Allow-Origin","*");
            resp.addHeader("Content-Type","application/json");

            JsonArrayBuilder allCustomers = Json.createArrayBuilder();


            while (rst.next()) {
                String orderID = rst.getString(1);
                String orderCusID = rst.getString(2);
                String orderDate = rst.getString(3);
//                String orderTotal = String.valueOf(rst.getInt(4));

                JsonObjectBuilder customer = Json.createObjectBuilder();

                customer.add("orderID",orderID);
                customer.add("orderCusID",orderCusID);
                customer.add("orderDate",orderDate);
//                customer.add("contact",contact);

                allCustomers.add(customer.build());
            }


            writer.print(allCustomers.build());


        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }*/
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JsonReader reader = Json.createReader(req.getReader());
        JsonObject jsonObject = reader.readObject();

        resp.addHeader("Content-Type","application/json");
        resp.addHeader("Access-Control-Allow-Origin","*");

        String orderId = jsonObject.getString("orderId");
        String orderDate = jsonObject.getString("orderDate");
        String customerId = jsonObject.getString("customerId");
        String itemCode = jsonObject.getString("itemCode");
        String qty = jsonObject.getString("qty");
        String unitPrice = jsonObject.getString("unitPrice");
        JsonArray orderDetails = jsonObject.getJsonArray("orderDetails");

        try {
            forName("com.mysql.jdbc.Driver");
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/company", "root", "1234");
            connection.setAutoCommit(false);

            PreparedStatement orderStatement = connection.prepareStatement("INSERT INTO orders VALUES(?,?,?)");
            orderStatement.setString(1, orderId);
            orderStatement.setString(2, orderDate);
            orderStatement.setString(3, customerId);

            int affectedRows = orderStatement.executeUpdate();
            if (affectedRows == 0) {
                connection.rollback();
                throw new RuntimeException("Failed to save the order");
            } else {
                System.out.println("Order Saved");

            }

            for (JsonValue orderDetailValue : orderDetails) {
                JsonObject orderDetailObject = orderDetailValue.asJsonObject();
                String detailItemCode = orderDetailObject.getString("itemCode");
                String detailQty = orderDetailObject.getString("qty");
                String detailUnitPrice = orderDetailObject.getString("unitPrice");

                PreparedStatement pstm = connection.prepareStatement("INSERT INTO orderDetails VALUES(?,?,?,?)");
                pstm.setString(1, orderId);
                pstm.setString(2, detailItemCode);
                pstm.setString(3, detailQty);
                pstm.setString(4, detailUnitPrice);

                affectedRows = pstm.executeUpdate();
                if (affectedRows == 0) {
                    connection.rollback();
                    throw new RuntimeException("Failed to save the order details");
                } else {
                    System.out.println("Order Details Saved for item: " + detailItemCode);

                    PreparedStatement updateItemStatement = connection.prepareStatement(
                            "UPDATE items SET quantity = quantity - ? WHERE code = ?");
                    updateItemStatement.setInt(1, Integer.parseInt(detailQty));
                    updateItemStatement.setString(2, detailItemCode);

                    affectedRows = updateItemStatement.executeUpdate();
                    if (affectedRows == 0) {
                        connection.rollback();
                        throw new RuntimeException("Failed to update item quantity");
                    } else {
                        System.out.println("Item quantity updated for item: " + detailItemCode);
                    }
                }
            }

            connection.commit();
            resp.setStatus(HttpServletResponse.SC_OK);
            JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
            objectBuilder.add("message", "Successfully Purchased Order.");
            objectBuilder.add("status", resp.getStatus());
            resp.getWriter().print(objectBuilder.build());


        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (SQLException e) {
            JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
            objectBuilder.add("message", "Failed to save the order.");
            objectBuilder.add("status", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print(objectBuilder.build());

        }
        
        
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.addHeader("Access-Control-Allow-Origin", "*");
        resp.addHeader("Access-Control-Allow-Headers", "Content-Type");
        resp.addHeader("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE");
    }
}
