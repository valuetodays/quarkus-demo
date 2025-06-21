package cn.valuetodays.api2.module.fortune.persist;

import cn.valuetodays.quarkus.commons.base.jpa.StatDateAndLongIdEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * .
 *
 * @author lei.liu
 * @since 2021-08-17 16:18
 */
@Entity
@Table(name = "fortune_quote_value")
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "指数估值")
public class QuoteValuePO extends StatDateAndLongIdEntity {
    @Column(name = "code")
    private String code;
    @Column(name = "pe_val")
    private Double peVal;
    @Column(name = "pb_val")
    private Double pbVal;
}
