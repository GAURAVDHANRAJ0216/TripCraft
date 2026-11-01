

import java.io.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

class TourBooking implements Serializable {
    private static final long serialVersionUID = 1L;

    protected int bookingId;
    protected String customerName, destination;
    protected int persons, totalSeats;
    protected LocalDate bookingDate, travelDate;
    protected double pricePerPerson;
    protected boolean cancelled = false;

    public TourBooking(int bookingId, String customerName, String destination,
                       int persons, LocalDate bookingDate, LocalDate travelDate,
                       double pricePerPerson, int totalSeats) {
        this.bookingId = bookingId;
        this.customerName = customerName;
        this.destination = destination;
        this.persons = persons;
        this.bookingDate = bookingDate;
        this.travelDate = travelDate;
        this.pricePerPerson = pricePerPerson;
        this.totalSeats = totalSeats;
    }

    public double calculateTotalCost() {
        return calculateFinalCost(pricePerPerson * persons);
    }

    protected double calculateFinalCost(double baseCost) {
        long days = ChronoUnit.DAYS.between(bookingDate, travelDate);
        double discount = (days >= 60 ? baseCost * 0.10 : 0)
                       + (persons >= 5 ? baseCost * 0.05 : 0);

        return (baseCost - discount) * 1.05;
    }

    public void display() {
        System.out.println("\nBooking ID       : " + bookingId);
        System.out.println("Customer Name    : " + customerName);
        System.out.println("Destination      : " + destination);
        System.out.println("Number of Persons: " + persons);
        System.out.println("Booking Date     : " + bookingDate);
        System.out.println("Travel Date      : " + travelDate);
        System.out.println("Price Per Person : Rs." + pricePerPerson);
        System.out.println("Total Cost       : Rs."
                + String.format("%.2f", calculateTotalCost()));
        System.out.println("Status           : "
                + (cancelled ? "Cancelled" : "Active"));
    }

    public int getBookingId() {
        return bookingId;
    }

    public String getDestination() {
        return destination;
    }

    public int getPersons() {
        return persons;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public LocalDate getTravelDate() {
        return travelDate;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void cancel() {
        cancelled = true;
    }
}

class InternationalBooking extends TourBooking {
    private static final long serialVersionUID = 1L;

    private final double visaFee;
    private final double forexMarkup;
    private final double travelInsurance;

    public InternationalBooking(int bookingId, String customerName,
                                String destination, int persons,
                                LocalDate bookingDate, LocalDate travelDate,
                                double pricePerPerson, int totalSeats,
                                double visaFee, double forexMarkup,
                                double travelInsurance) {
        super(bookingId, customerName, destination, persons,
              bookingDate, travelDate, pricePerPerson, totalSeats);

        this.visaFee = visaFee;
        this.forexMarkup = forexMarkup;
        this.travelInsurance = travelInsurance;
    }

    @Override
    public double calculateTotalCost() {
        double baseCost = (pricePerPerson + visaFee + forexMarkup
                + travelInsurance) * persons;

        return calculateFinalCost(baseCost);
    }

    @Override
    public void display() {
        super.display();
        System.out.println("Booking Type     : International");
        System.out.println("Visa Fee         : Rs." + visaFee);
        System.out.println("Forex Markup     : Rs." + forexMarkup);
        System.out.println("Travel Insurance : Rs." + travelInsurance);
    }
}

final class BookingStorage {
    private static final String FILE_NAME = "tourbookings.dat";

    private BookingStorage() {}

    @SuppressWarnings("unchecked")
    public static ArrayList<TourBooking> load() {
        File file = new File(FILE_NAME);

        if (!file.exists()) {
            return new ArrayList<>();
        }

        try (ObjectInputStream in =
                     new ObjectInputStream(new FileInputStream(file))) {
            return (ArrayList<TourBooking>) in.readObject();
        } catch (IOException | ClassNotFoundException | ClassCastException e) {
            System.out.println("Error loading records: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public static void save(ArrayList<TourBooking> bookings) {
        try (ObjectOutputStream out =
                     new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            out.writeObject(bookings);
        } catch (IOException e) {
            System.out.println("Error saving records: " + e.getMessage());
        }
    }
}

final class BookingService {
    private BookingService() {}

    public static TourBooking find(ArrayList<TourBooking> bookings, int id) {
        for (TourBooking booking : bookings) {
            if (booking.getBookingId() == id) {
                return booking;
            }
        }
        return null;
    }

    public static boolean hasSeats(ArrayList<TourBooking> bookings,
                                   String destination,
                                   int persons,
                                   int capacity) {
        int used = 0;

        for (TourBooking booking : bookings) {
            if (booking.getDestination().equalsIgnoreCase(destination)
                    && !booking.isCancelled()) {
                used += booking.getPersons();
            }
        }

        return used + persons <= capacity;
    }

    public static void add(ArrayList<TourBooking> bookings, Scanner sc) {
        try {
            System.out.println("\n1. Domestic Booking");
            System.out.println("2. International Booking");
            System.out.print("Enter booking type: ");
            int type = sc.nextInt();
            sc.nextLine();

            System.out.print("Enter Booking ID: ");
            int id = sc.nextInt();
            sc.nextLine();

            if (find(bookings, id) != null) {
                System.out.println("Booking ID already exists!");
                return;
            }

            System.out.print("Enter Customer Name: ");
            String name = sc.nextLine().trim();

            System.out.print("Enter Destination: ");
            String destination = sc.nextLine().trim();

            System.out.print("Enter Number of Persons: ");
            int persons = sc.nextInt();

            System.out.print("Enter Package Capacity: ");
            int seats = sc.nextInt();

            System.out.print("Enter Price Per Person: ");
            double price = sc.nextDouble();

            LocalDate bookingDate = LocalDate.now();
            System.out.println("Booking Date: " + bookingDate);

            System.out.print("Enter Travel Date (yyyy-mm-dd): ");
            LocalDate travelDate = LocalDate.parse(sc.next());

            if (name.isEmpty() || destination.isEmpty()
                    || persons <= 0 || seats <= 0 || price <= 0) {
                System.out.println("Invalid booking details!");
                return;
            }

            if (!travelDate.isAfter(bookingDate)) {
                System.out.println("Travel date must be after booking date!");
                return;
            }

            if (!hasSeats(bookings, destination, persons, seats)) {
                System.out.println("Not enough seats available!");
                return;
            }

            TourBooking booking;

            if (type == 1) {
                booking = new TourBooking(id, name, destination, persons,
                        bookingDate, travelDate, price, seats);
            } else if (type == 2) {
                System.out.print("Enter Visa Fee Per Person: ");
                double visa = sc.nextDouble();

                System.out.print("Enter Forex Markup Per Person: ");
                double forex = sc.nextDouble();

                System.out.print("Enter Travel Insurance Per Person: ");
                double insurance = sc.nextDouble();

                if (visa < 0 || forex < 0 || insurance < 0) {
                    System.out.println("Invalid additional charges!");
                    return;
                }

                booking = new InternationalBooking(id, name, destination,
                        persons, bookingDate, travelDate, price, seats,
                        visa, forex, insurance);
            } else {
                System.out.println("Invalid booking type!");
                return;
            }

            bookings.add(booking);
            BookingStorage.save(bookings);

            System.out.println("\nBooking added successfully!");
            booking.display();

        } catch (InputMismatchException e) {
            System.out.println("Please enter valid input!");
            sc.nextLine();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void cancel(ArrayList<TourBooking> bookings, Scanner sc) {
        System.out.print("Enter Booking ID: ");
        int id = sc.nextInt();

        TourBooking booking = find(bookings, id);

        if (booking == null || booking.isCancelled()) {
            System.out.println("Active booking not found!");
            return;
        }

        long days = ChronoUnit.DAYS.between(
                LocalDate.now(), booking.getTravelDate());

        double rate;
        if (days >= 30) {
            rate = 90;
        } else if (days >= 15) {
            rate = 50;
        } else if (days >= 1) {
            rate = 25;
        } else {
            rate = 0;
        }

        double total = booking.calculateTotalCost();
        double refund = total * rate / 100;

        booking.cancel();
        BookingStorage.save(bookings);

        System.out.println("\nBooking Cancelled Successfully!");
        System.out.println("Total Amount : Rs."
                + String.format("%.2f", total));
        System.out.println("Refund Rate  : " + rate + "%");
        System.out.println("Refund Amount: Rs."
                + String.format("%.2f", refund));
    }
}

final class BookingReports {
    private BookingReports() {}

    public static void displayAll(ArrayList<TourBooking> bookings) {
        if (bookings.isEmpty()) {
            System.out.println("No bookings available.");
            return;
        }

        for (TourBooking booking : bookings) {
            booking.display();
            System.out.println("----------------------------");
        }
    }

    public static void search(ArrayList<TourBooking> bookings, Scanner sc) {
        System.out.print("Enter Booking ID: ");
        TourBooking booking =
                BookingService.find(bookings, sc.nextInt());

        if (booking != null) {
            booking.display();
        } else {
            System.out.println("Booking not found!");
        }
    }

    public static void delete(ArrayList<TourBooking> bookings, Scanner sc) {
        System.out.print("Enter Booking ID to delete: ");
        TourBooking booking =
                BookingService.find(bookings, sc.nextInt());

        if (booking != null) {
            bookings.remove(booking);
            BookingStorage.save(bookings);
            System.out.println("Booking deleted successfully!");
        } else {
            System.out.println("Booking not found!");
        }
    }

    public static void popularDestinations(
            ArrayList<TourBooking> bookings) {
        HashMap<String, Integer> popularity = new HashMap<>();

        for (TourBooking booking : bookings) {
            if (!booking.isCancelled()) {
                popularity.merge(booking.getDestination(),
                        booking.getPersons(), Integer::sum);
            }
        }

        List<Map.Entry<String, Integer>> list =
                new ArrayList<>(popularity.entrySet());

        list.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        System.out.println("\nTOP 3 POPULAR DESTINATIONS");

        int count = 0;
        for (Map.Entry<String, Integer> entry : list) {
            if (count == 3) {
                break;
            }

            System.out.println((count + 1) + ". "
                    + entry.getKey() + " - "
                    + entry.getValue() + " travelers");
            count++;
        }

        if (count == 0) {
            System.out.println("No active bookings available.");
        }
    }
}

public class TripCraft {
    public static void main(String[] args) {
        ArrayList<TourBooking> bookings = BookingStorage.load();
        Scanner sc = new Scanner(System.in);
        int choice = 0;

        do {
            System.out.println("\n================================");
            System.out.println("       TRIPCRAFT SYSTEM");
            System.out.println("================================");
            System.out.println("1. Add Booking");
            System.out.println("2. Display All Bookings");
            System.out.println("3. Search Booking");
            System.out.println("4. Delete Booking");
            System.out.println("5. Cancel Booking");
            System.out.println("6. Top 3 Popular Destinations");
            System.out.println("7. Exit");
            System.out.print("Enter your choice: ");

            try {
                choice = sc.nextInt();

                switch (choice) {
                    case 1:
                        BookingService.add(bookings, sc);
                        break;
                    case 2:
                        BookingReports.displayAll(bookings);
                        break;
                    case 3:
                        BookingReports.search(bookings, sc);
                        break;
                    case 4:
                        BookingReports.delete(bookings, sc);
                        break;
                    case 5:
                        BookingService.cancel(bookings, sc);
                        break;
                    case 6:
                        BookingReports.popularDestinations(bookings);
                        break;
                    case 7:
                        BookingStorage.save(bookings);
                        System.out.println(
                                "Data saved. Thank you for using TripCraft!");
                        break;
                    default:
                        System.out.println("Invalid choice!");
                }
            } catch (InputMismatchException e) {
                System.out.println("Enter a valid menu choice!");
                sc.nextLine();
            }
        } while (choice != 7);

        sc.close();
    }
}