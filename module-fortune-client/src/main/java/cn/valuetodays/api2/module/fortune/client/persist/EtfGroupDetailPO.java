package cn.valuetodays.api2.module.fortune.client.persist;

import cn.valuetodays.quarkus.commons.base.jpa.JpaCrudLongIdBasePersist;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * etf组-详情
 *
 * @author lei.liu
 * @since 2023-05-31 18:03
 */
@Table(name = "fortune_etf_group_detail")
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode(callSuper = true)
@Data
public class EtfGroupDetailPO extends JpaCrudLongIdBasePersist {
    @Column(name = "group_id")
    private Long groupId;
    @Column(name = "code")
    private String code;
}
