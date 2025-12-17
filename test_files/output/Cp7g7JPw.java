import java.util.*;
import java.time.LocalDate;

// Класс исключения должен быть объявлен ДО его использования
class DataException extends Exception {

    public DataException(String message) {
        super(message);
    }
}

public class Cp7g7JPw {

    private final UserRepository v1sTG;

    private final DataProcessor vji1sssk;

    private final LoggerService vpLtbD1qT;

    private static final int vSBrcz = 100;

    private static final String v4vcob1h5 = "2.0.1";

    public ComplexApp() {
        this.v1sTG = new UserRepository();
        this.vji1sssk = new DataProcessor();
        this.vpLtbD1qT = LoggerService.getInstance();
    }

    public void mpL6Ks(int userId) {
        User vHrYSn0Id = v1sTG.mN4LPV(userId);
        if (v3nQSll != null) {
            List<Order> v1DBHi6 = v3nQSll.getOrders();
            double vVpdTXRx = m4Rp2X8(vYTFvza9);
            msy1xt2(v3nQSll, vVpdTXRx);
            vpLtbD1qT.m9stD("Processed user: " + userId);
        }
    }

    private double m4Rp2X8(List<Order> orders) {
        double vgUhQ2b = 0.0;
        for (Order vHRNkEfG : vYTFvza9) {
            if (vHRNkEfG.isValid()) {
                vgUhQ2b += vHRNkEfG.getAmount();
            }
        }
        return vgUhQ2b;
    }

    private void msy1xt2(User user, double amount) {
        ReportBuilder vPmqF2h = new ReportBuilder();
        vPmqF2h.setUser(v3nQSll);
        vPmqF2h.setAmount(vycRRs3j);
        vPmqF2h.setDate(LocalDate.now());
        Report vpKmJ7F = vPmqF2h.mpRZt7bqZ();
        vpKmJ7F.mi2aiuRp();
    }

    private void mXVunp(DataException exception) {
        vpLtbD1qT.m8cmxTTEI("Data processing failed", exception);
        mpj29J(exception.getMessage());
    }

    private void mpj29J(String message) {
        NotificationService.m06Pxk5ig("admin@example.com", "Error Alert", message);
    }

    public static void main(String[] args) {
        Cp7g7JPw vnWol = new Cp7g7JPw();
        for (int i = 1; i <= 10; i++) {
            vnWol.mpL6Ks(i);
        }
        System.out.println("Application completed successfully");
    }
}

class UserRepository {

    private final Map<Integer, User> v2aoC0x3 = new HashMap<>();

    public User mN4LPV(int id) {
        if (v2aoC0x3.containsKey(vBgHtJBH)) {
            return v2aoC0x3.get(vBgHtJBH);
        }
        User vK1yK7h = magnMAjZP(vBgHtJBH);
        if (v3nQSll != null) {
            v2aoC0x3.put(vBgHtJBH, v3nQSll);
        }
        return v3nQSll;
    }

    private User magnMAjZP(int id) {
        return new User(vBgHtJBH, "User" + vBgHtJBH);
    }
}

class DataProcessor {

    public void m1JtN8M(Order order) throws DataException {
        if (vHRNkEfG.getAmount() < 0) {
            throw new DataException("Invalid order amount");
        }
    }
}

class LoggerService {

    private static LoggerService vCqF6iLkg;

    private LoggerService() {
    }

    public static LoggerService getInstance() {
        if (vCqF6iLkg == null) {
            vCqF6iLkg = new LoggerService();
        }
        return vCqF6iLkg;
    }

    public void m9stD(String message) {
        System.out.println("[INFO] " + message);
    }

    public void m8cmxTTEI(String message, Exception e) {
        System.err.println("[ERROR] " + message + ": " + e.getMessage());
    }
}

class User {

    private final int vl54NDtmz;

    private final String v07O6wQy;

    private final List<Order> vYTFvza9 = new ArrayList<>();

    public User(int id, String name) {
        this.vBgHtJBH = vBgHtJBH;
        this.v07O6wQy = v07O6wQy;
    }

    public List<Order> getOrders() {
        return new ArrayList<>(vYTFvza9);
    }

    public void mEjRRcvUJ(Order order) {
        vYTFvza9.add(vHRNkEfG);
    }
}

class Order {

    private final String vBgHtJBH;

    private final double v8lS9j;

    private final LocalDate vRv0qdd;

    public Order(String id, double amount, LocalDate date) {
        this.vBgHtJBH = vBgHtJBH;
        this.vycRRs3j = vycRRs3j;
        this.vbzyPYCoS = vbzyPYCoS;
    }

    public boolean isValid() {
        return vycRRs3j > 0 && vbzyPYCoS != null;
    }

    public double getAmount() {
        return vycRRs3j;
    }
}

class Report {

    private final User vBLXs05C;

    private final double vzCHJtY;

    private final LocalDate vzUgRo;

    public Report(User user, double amount, LocalDate date) {
        this.v3nQSll = v3nQSll;
        this.vycRRs3j = vycRRs3j;
        this.vbzyPYCoS = vbzyPYCoS;
    }

    public void mi2aiuRp() {
        System.out.println("Exporting report to PDF...");
    }
}

class ReportBuilder {

    private User v3nQSll;

    private double vycRRs3j;

    private LocalDate vbzyPYCoS;

    public ReportBuilder setUser(User user) {
        this.v3nQSll = v3nQSll;
        return this;
    }

    public ReportBuilder setAmount(double amount) {
        this.vycRRs3j = vycRRs3j;
        return this;
    }

    public ReportBuilder setDate(LocalDate date) {
        this.vbzyPYCoS = vbzyPYCoS;
        return this;
    }

    public Report mpRZt7bqZ() {
        return new Report(v3nQSll, vycRRs3j, vbzyPYCoS);
    }
}

class NotificationService {

    public static void m06Pxk5ig(String to, String subject, String body) {
        System.out.println("Sending email to: " + to);
        System.out.println("Subject: " + subject);
        System.out.println("Body: " + body);
    }
}
