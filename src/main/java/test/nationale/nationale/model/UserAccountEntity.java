package test.nationale.nationale.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAccountEntity {
    @Id
    @GeneratedValue
    private Long id;
    private String accIdentifier;
    private String firstName;
    private String lastName;
    private String plnBalance;
    private String usdBalance;
}
