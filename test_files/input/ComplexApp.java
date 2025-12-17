
import java.util.*;
import java.time.LocalDate;

// Класс исключения должен быть объявлен ДО его использования
class DataException extends Exception {
    public DataException(String message) {
        super(message);
    }
}

public class ComplexApp {

    private final UserRepository userRepository;
    private final DataProcessor dataProcessor;
    private final LoggerService loggerService;

    private static final int MAX_USERS = 100;
    private static final String APP_VERSION = "2.0.1";

    public ComplexApp() {
        this.userRepository = new UserRepository();
        this.dataProcessor = new DataProcessor();
        this.loggerService = LoggerService.getInstance();
    }

    public void processUserData(int userId) {
        User user = userRepository.findById(userId);
        if (user != null) {
            List<Order> orders = user.getOrders();
            double totalAmount = calculateTotalAmount(orders);
            generateReport(user, totalAmount);
            loggerService.log("Processed user: " + userId);
        }
    }

    private double calculateTotalAmount(List<Order> orders) {
        double sum = 0.0;
        for (Order order : orders) {
            if (order.isValid()) {
                sum += order.getAmount();
            }
        }
        return sum;
    }

    private void generateReport(User user, double amount) {
        ReportBuilder builder = new ReportBuilder();
        builder.setUser(user);
        builder.setAmount(amount);
        builder.setDate(LocalDate.now());

        Report report = builder.build();
        report.exportToPDF();
    }

    private void handleException(DataException exception) {
        loggerService.error("Data processing failed", exception);
        sendAlert(exception.getMessage());
    }

    private void sendAlert(String message) {
        NotificationService.send("admin@example.com", "Error Alert", message);
    }

    public static void main(String[] args) {
        ComplexApp app = new ComplexApp();

        for (int i = 1; i <= 10; i++) {
            app.processUserData(i);
        }

        System.out.println("Application completed successfully");
    }
}

class UserRepository {
    private final Map<Integer, User> userCache = new HashMap<>();

    public User findById(int id) {
        if (userCache.containsKey(id)) {
            return userCache.get(id);
        }

        User user = loadFromDatabase(id);
        if (user != null) {
            userCache.put(id, user);
        }
        return user;
    }

    private User loadFromDatabase(int id) {
        return new User(id, "User" + id);
    }
}

class DataProcessor {
    public void validate(Order order) throws DataException {
        if (order.getAmount() < 0) {
            throw new DataException("Invalid order amount");
        }
    }
}

class LoggerService {
    private static LoggerService instance;

    private LoggerService() {}

    public static LoggerService getInstance() {
        if (instance == null) {
            instance = new LoggerService();
        }
        return instance;
    }

    public void log(String message) {
        System.out.println("[INFO] " + message);
    }

    public void error(String message, Exception e) {
        System.err.println("[ERROR] " + message + ": " + e.getMessage());
    }
}

class User {
    private final int id;
    private final String name;
    private final List<Order> orders = new ArrayList<>();

    public User(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public List<Order> getOrders() {
        return new ArrayList<>(orders);
    }

    public void addOrder(Order order) {
        orders.add(order);
    }
}

class Order {
    private final String id;
    private final double amount;
    private final LocalDate date;

    public Order(String id, double amount, LocalDate date) {
        this.id = id;
        this.amount = amount;
        this.date = date;
    }

    public boolean isValid() {
        return amount > 0 && date != null;
    }

    public double getAmount() {
        return amount;
    }
}

class Report {
    private final User user;
    private final double amount;
    private final LocalDate date;

    public Report(User user, double amount, LocalDate date) {
        this.user = user;
        this.amount = amount;
        this.date = date;
    }

    public void exportToPDF() {
        System.out.println("Exporting report to PDF...");
    }
}

class ReportBuilder {
    private User user;
    private double amount;
    private LocalDate date;

    public ReportBuilder setUser(User user) {
        this.user = user;
        return this;
    }

    public ReportBuilder setAmount(double amount) {
        this.amount = amount;
        return this;
    }

    public ReportBuilder setDate(LocalDate date) {
        this.date = date;
        return this;
    }

    public Report build() {
        return new Report(user, amount, date);
    }
}

class NotificationService {
    public static void send(String to, String subject, String body) {
        System.out.println("Sending email to: " + to);
        System.out.println("Subject: " + subject);
        System.out.println("Body: " + body);
    }
}