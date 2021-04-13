package api;

public class User {
    private String name;
    private String password;
    private double amount;

    public User(String name, String password, double amount) {
        this.name = name;
        this.password = password;
        this.amount = amount;
    }

    public String getName() {
        return name;
    }

    public boolean checkPassword(String password) {
        return this.password.equals(password);
    }

    public double getAmount() {
        return amount;
    }

    public double deposit(double amount) {
        this.amount += amount;
        return this.amount;
    }
}
