package jsp.servlet;

import javax.json.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;

//http://localhost:8080/pos_one/customer
//http://localhost:8080/pos_one/pages/customer? 404
//http://localhost:8080/customer? 404

//http://localhost:8080/pos_one/pages/customer//
//http:://localhost:8080/pos_one/pages/customer
//http:://localhost:8080/pos_one/pages/customer

@WebServlet(urlPatterns = {"/pages/customer"})
public class CustomerServletAPI extends HttpServlet {

    public CustomerServletAPI(){
        System.out.println("Customer Servlet");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/company", "root", "1234");
            PreparedStatement pstm = connection.prepareStatement("select * from customer");
            ResultSet rst = pstm.executeQuery();

            resp.addHeader("Content-Type","application/json");
            resp.addHeader("Access-Control-Allow-Origin","*");


            JsonArrayBuilder allCustomer =Json.createArrayBuilder();
            while (rst.next()) {
                
                String id = rst.getString(1);
                String name = rst.getString(2);
                String address = rst.getString(3);

                JsonObjectBuilder customerBuilder = Json.createObjectBuilder();
                customerBuilder.add("id",id);
                customerBuilder.add("name",name);
                customerBuilder.add("address",address);
                allCustomer.add(customerBuilder.build());
            }
            
            resp.getWriter().print(allCustomer.build());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String cusID = req.getParameter("cusID");
        String cusName = req.getParameter("cusName");
        String cusAddress = req.getParameter("cusAddress");
        String cusSalary = req.getParameter("cusSalary");
        String option = req.getParameter("option");

//        JsonReader reader = Json.createReader(req.getReader());
//        JsonObject customerObject = reader.readObject();
//
//        String cusID = customerObject.getString("cusID");
//        String cusName = customerObject.getString("cusName");
//        String cusAddress = customerObject.getString("cusAddress");

        resp.addHeader("Content-Type","application/json");
        resp.addHeader("Access-Control-Allow-Origin","*");

        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/company", "root", "1234");

                    PreparedStatement pstm = connection.prepareStatement("insert into customer values(?,?,?)");
                    pstm.setObject(1, cusID);
                    pstm.setObject(2, cusName);
                    pstm.setObject(3, cusAddress);
                    if (pstm.executeUpdate() > 0) {
//                        resp.getWriter().println("Customer Added..!");

                        JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
                        objectBuilder.add("state", "Ok");
                        objectBuilder.add("message", "Successfully Added...!");
                        objectBuilder.add("data", "[]");
                        resp.getWriter().print(objectBuilder.build());
                    }


        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (SQLException e) {

            JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
            objectBuilder.add("state", "Error");
            objectBuilder.add("message", e.getMessage());
            objectBuilder.add("data", "");
            resp.setStatus(400);
            resp.getWriter().print(objectBuilder.build());

//            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        JsonReader reader = Json.createReader(req.getReader());
        JsonObject customerObject = reader.readObject();

//        String cusID = req.getParameter("cusID");
//        String cusName = req.getParameter("cusName");
//        String cusAddress = req.getParameter("cusAddress");
//        String cusSalary = req.getParameter("cusSalary");
//        String option = req.getParameter("option");

        String cusID = customerObject.getString("id");
        String cusName = customerObject.getString("name");
        String cusAddress = customerObject.getString("address");

        resp.addHeader("Content-Type","application/json");
        resp.addHeader("Access-Control-Allow-Origin","*");

        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/company", "root", "1234");

                    PreparedStatement pstm3 = connection.prepareStatement("update customer set name=?,address=? where id=?");
                    pstm3.setObject(3, cusID);
                    pstm3.setObject(1, cusName);
                    pstm3.setObject(2, cusAddress);

                    if (pstm3.executeUpdate() > 0) {
                        JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
                        objectBuilder.add("state", "Ok");
                        objectBuilder.add("message", "Successfully Updated...!");
                        objectBuilder.add("data", "[]");
                        resp.getWriter().print(objectBuilder.build());
                    }



        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (SQLException e) {

            JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
            objectBuilder.add("state", "Error");
            objectBuilder.add("message", e.getMessage());
            objectBuilder.add("data", "");
            resp.setStatus(400);
            resp.getWriter().print(objectBuilder.build());


        }

    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String cusID = req.getParameter("id");

        resp.addHeader("Content-Type","application/json");
        resp.addHeader("Access-Control-Allow-Origin","*");

        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/company", "root", "1234");


                    PreparedStatement pstm2 = connection.prepareStatement("delete from customer where id=?");
                    pstm2.setObject(1, cusID);

                    if (pstm2.executeUpdate() > 0) {
                        JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
                        objectBuilder.add("state", "Ok");
                        objectBuilder.add("message", "Successfully Delete...!");
                        objectBuilder.add("data", "[]");
                        resp.getWriter().print(objectBuilder.build());
                    }


        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (SQLException e) {

            JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
            objectBuilder.add("state", "Error");
            objectBuilder.add("message", e.getMessage());
            objectBuilder.add("data", "");
            resp.setStatus(400);
            resp.getWriter().print(objectBuilder.build());

//            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.addHeader("Access-Control-Allow-Origin","*");
        resp.addHeader("Access-Control-Allow-Methods","PUT");
        resp.addHeader("Access-Control-Allow-Methods","DELETE");
        resp.addHeader("Access-Control-Allow-Headers","Content-type");
    }
}
