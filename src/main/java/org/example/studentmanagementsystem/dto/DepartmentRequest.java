// dto/DepartmentRequest.java
public record DepartmentRequest(
        @NotBlank(message = "Department name is required") String name,
        @NotBlank(message = "Department code is required")
        @Size(max = 10, message = "Code must be at most 10 characters") String code) {}

// dto/DepartmentResponse.java
public record DepartmentResponse(Integer id, String name, String code) implements Serializable {}

// dto/CourseRequest.java
public record CourseRequest(
        @NotBlank(message = "Title is required") String title,
        @NotBlank(message = "Code is required")
        @Size(max = 15, message = "Code must be at most 15 characters") String code,
        @Min(value = 1, message = "Credits must be 1-10") @Max(value = 10, message = "Credits must be 1-10") int credits,
        @NotNull(message = "departmentId is required") Integer departmentId) {}

// dto/CourseResponse.java
public record CourseResponse(Integer id, String title, String code, int credits,
                             Integer departmentId, String departmentName) implements Serializable {}