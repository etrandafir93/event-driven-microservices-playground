package io.github.etr.playground.domain.loyalty;

import static java.util.Arrays.stream;
import static java.util.Comparator.reverseOrder;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;
import org.springframework.util.Assert;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class TieredMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Length(min = 3, max = 20)
    @Column(unique = true)
    private String username;

    @Min(0)
    private int points;

    @NotNull
    private Tier tier;

    public TieredMember(String username) {
        this.username = username;
        this.points = 0;
        this.tier = Tier.BRONZE;
    }

    @RequiredArgsConstructor
    enum Tier {
        BRONZE(0),
        SILVER(300),
        GOLD(500),
        PLATINUM(1000);

        private final int minPoints;

        public static Tier forPoints(int points) {
            Assert.isTrue(points > 0, "points must be greater than 0");
            return stream(values())
                .sorted(reverseOrder())
                .filter(it -> it.minPoints <= points)
                .findFirst()
                .orElseThrow();
        }
    }

    public void earn(int newPoints) {
        Assert.isTrue(newPoints > 0, "newPoints must be greater than 0");
        this.points += newPoints;
        this.tier = Tier.forPoints(this.points);
    }

}