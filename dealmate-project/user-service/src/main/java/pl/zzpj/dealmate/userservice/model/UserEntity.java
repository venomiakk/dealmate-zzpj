package pl.zzpj.dealmate.userservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "users")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true)
    private Long id;

    @Column(nullable = false, unique = true)
    @NotBlank
    @Size(min = 3, max = 20)
    private String username;

    @Column(nullable = false, unique = true)
    @NotBlank
    @Size(max = 50)
    private String email;

    @Column(nullable = false)
    @NotBlank
    private String password;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ERole role;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDate createdAt = LocalDate.now();

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "country_code")
    @Enumerated(EnumType.STRING)
    private ECountryCodes countryCode;

    @Column(name = "credits")
    private Long credits = 10000L;

    public UserEntity(@NotBlank @Size(min = 3, max = 20) String username, @NotBlank @Size(max = 50) String email,
                      @NotBlank String password) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = ERole.ROLE_USER;
    }


    public UserEntity() {

    }

    public UserEntity(String username, String lastName, String firstName, ECountryCodes cc) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.countryCode = cc;
    }
}
