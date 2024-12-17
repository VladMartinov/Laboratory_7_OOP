import java.sql.*;

public class JdbcClass {
    public static void main(String[] args) {
        Connection connection = null;
        try {
            // 1. Открытие соединения с БД
            String url = "jdbc:postgresql://localhost:5432/laboratory_oop";
            String user = "postgres";
            String password = "...";

            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(url, user, password);

            // 2. Выполнение запросов
            insertData(connection);
            selectData(connection);

            createPassengerAndTicket(connection, 1, "Jane", "Doe", "+19876543210", 1, 2000);
            selectTickets(connection);
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            // 3. Закрытие соединения (в finally чтобы гарантировать закрытие)
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private static void createPassengerAndTicket(Connection conn, int tripId, String firstName, String lastName, String phoneNumber, int seatNumber, int ticketPrice) throws SQLException {
        //Отключаем автоматический коммит
        conn.setAutoCommit(false);

        try (PreparedStatement pstmt = conn.prepareStatement("INSERT INTO PASSENGERS (first_name, last_name, phone_number) VALUES (?, ?, ?)", new String[] {"passenger_id"})){
            // 1. Создание пассажира
            pstmt.setString(1, firstName);
            pstmt.setString(2, lastName);
            pstmt.setString(3, phoneNumber);
            pstmt.executeUpdate();

            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            int passengerId = -1;
            if(generatedKeys.next()){
                passengerId = generatedKeys.getInt(1);
            }

            if(passengerId == -1){
                throw new SQLException("Ошибка получения passengerId");
            }

            // 2. Создание билета
            try (PreparedStatement pstmt2 = conn.prepareStatement("INSERT INTO TICKETS (trip_id, passenger_id, seat_number, purchase_date) VALUES (?, ?, ?, NOW())")) {
                pstmt2.setInt(1, tripId);
                pstmt2.setInt(2, passengerId);
                pstmt2.setInt(3, seatNumber);
                pstmt2.executeUpdate();
            }

            // 3. Обновление цены билета
            try (PreparedStatement pstmt3 = conn.prepareStatement("UPDATE TRIPS SET ticket_price = ? WHERE trip_id = ?")){
                pstmt3.setInt(1, ticketPrice);
                pstmt3.setInt(2, tripId);
                pstmt3.executeUpdate();
            }

            // 4. Коммит транзакции
            conn.commit();
            System.out.println("Пассажир и билет успешно созданы.");
        } catch (SQLException ex) {
            // 5. Откат транзакции
            conn.rollback();
            ex.printStackTrace();
            System.err.println("Транзакция отменена");
            throw ex;
        }  finally {
            conn.setAutoCommit(true);
        }
    }

    private static void insertData(Connection connection) throws SQLException {
        // Вставляем данные в таблицы
        try (Statement stmt = connection.createStatement()) {
            // Пример вставки в таблицу ROUTES
            stmt.executeUpdate("INSERT INTO routes (departure_city, destination_city, distance_km) VALUES ('Москва', 'Санкт-Петербург', 700.0)");
            stmt.executeUpdate("INSERT INTO routes (departure_city, destination_city, distance_km) VALUES ('Санкт-Петербург', 'Великий Новгород', 200.0)");

            // Пример вставки в таблицу BUSES
            stmt.executeUpdate("INSERT INTO buses (model, capacity) VALUES ('Mercedes-Benz Tourismo', 50)");
            stmt.executeUpdate("INSERT INTO buses (model, capacity) VALUES ('Scania Irizar', 45)");

            // Пример вставки в таблицу TRIPS
            stmt.executeUpdate("INSERT INTO trips (route_id, bus_id, departure_time, arrival_time, ticket_price) VALUES (1, 1, '2024-11-02 08:00:00', '2024-11-02 17:00:00', 1500.0)");
            stmt.executeUpdate("INSERT INTO trips (route_id, bus_id, departure_time, arrival_time, ticket_price) VALUES (2, 2, '2024-11-02 10:00:00', '2024-11-02 13:00:00', 800.0)");

            System.out.println("Данные успешно добавлены.");
        }
    }

    private static void selectData(Connection connection) throws SQLException {
        try (Statement stmt = connection.createStatement();
             ResultSet rset = stmt.executeQuery("SELECT route_id, departure_city, destination_city FROM routes")) {

            System.out.println("Маршруты:");
            while (rset.next()) {
                System.out.println(rset.getInt("route_id") + " | " +
                        rset.getString("departure_city") + " - " +
                        rset.getString("destination_city"));
            }
        }
    }
    private static void selectTickets(Connection connection) throws SQLException {
        try (Statement stmt = connection.createStatement();
             ResultSet rset = stmt.executeQuery("SELECT t.ticket_id, p.first_name, p.last_name, tp.departure_time, tp.ticket_price FROM tickets t JOIN passengers p ON t.passenger_id = p.passenger_id JOIN trips tp ON t.trip_id = tp.trip_id")) {

            System.out.println("Билеты:");
            while (rset.next()) {
                System.out.println(rset.getInt("ticket_id") + " | " +
                        rset.getString("first_name") + " " +
                        rset.getString("last_name") + " | " +
                        rset.getTimestamp("departure_time")+ " | " +
                        rset.getString("ticket_price"));
            }
        }
    }
}