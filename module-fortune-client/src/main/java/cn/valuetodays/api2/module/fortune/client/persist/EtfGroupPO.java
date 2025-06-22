package cn.valuetodays.api2.module.fortune.client.persist;

import cn.valuetodays.quarkus.commons.base.jpa.JpaCrudLongIdBasePersist;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * etf组
 *
 * @author lei.liu
 * @since 2023-05-31 18:03
 */
@Table(name = "fortune_etf_group")
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode(callSuper = true)
@Data
public class EtfGroupPO extends JpaCrudLongIdBasePersist {
    @Column(name = "name")
    private String name;
    @Column(name = "enable_flag")
    private boolean enableFlag;
    @Column(name = "tn")
    private int tn;
    @Column(name = "basic_info")
    private String basicInfo;
}
