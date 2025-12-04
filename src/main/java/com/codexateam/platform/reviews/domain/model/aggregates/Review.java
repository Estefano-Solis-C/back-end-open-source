package com.codexateam.platform.reviews.domain.model.aggregates;

import com.codexateam.platform.reviews.domain.model.commands.CreateReviewCommand;
import com.codexateam.platform.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Represents the Review aggregate root in the Reviews bounded context.
 * Based on the 'reviews' table in db.json.
 * The 'date' field from db.json is automatically handled by AuditableAbstractAggregateRoot as 'createdAt'.
 */
@NoArgsConstructor
@Getter
@Entity
@Table(name = "reviews")
public class Review extends AuditableAbstractAggregateRoot<Review> {

    @Column(nullable = false)
    private Long vehicleId;

    @Column(nullable = false)
    private Long renterId;

    @Min(1)
    @Max(5)
    @Column(nullable = false)
    private Integer rating;

    @Column(length = 2000)
    private String comment;

    public Review(CreateReviewCommand command) {
        this.vehicleId = command.vehicleId();
        this.renterId = command.renterId();
        this.rating = command.rating();
        this.comment = command.comment();
    }
}
