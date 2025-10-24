public class Example {

    // Simple method - Complexity: 1
    public int add(int a, int b) {
        return a + b;
    }

    // Method with conditions - Complexity: 4
    public String categorizeAge(int age) {
        if (age < 0) {
            return "invalid";
        } else if (age < 13) {
            return "child";
        } else if (age < 20) {
            return "teenager";
        } else {
            return "adult";
        }
    }

    // Method with loops and conditions - Complexity: 6
    public int sumEvenNumbers(int[] numbers) {
        int sum = 0;
        for (int num : numbers) {
            if (num % 2 == 0) {
                if (num > 0) {
                    sum += num;
                }
            }
        }
        return sum;
    }

    // Complex method - Complexity: 9
    public boolean validateUser(String username, String password, boolean isAdmin) {
        if (username == null || username.isEmpty()) {
            return false;
        }

        if (password == null || password.length() < 8) {
            return false;
        }

        if (isAdmin && !username.startsWith("admin_")) {
            return false;
        }

        for (char c : password.toCharArray()) {
            if (Character.isDigit(c)) {
                return true;
            }
        }

        return false;
    }

    // Method with logical operators - Complexity: 5
    public boolean canAccessResource(User user, Resource resource) {
        if (user == null || resource == null) {
            return false;
        }

        return (user.isAdmin() || resource.isPublic()) &&
               !resource.isDeleted() &&
               user.hasPermission(resource);
    }
}

class User {
    public boolean isAdmin() { return false; }
    public boolean hasPermission(Resource r) { return false; }
}

class Resource {
    public boolean isPublic() { return false; }
    public boolean isDeleted() { return false; }
}
