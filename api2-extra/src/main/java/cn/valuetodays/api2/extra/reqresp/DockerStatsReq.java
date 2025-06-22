package cn.valuetodays.api2.extra.reqresp;

import java.io.Serializable;
import java.time.LocalDateTime;

import cn.valuetodays.api2.extra.persist.DockerStatsPersist;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * .
 *
 * @author lei.liu
 * @since 2025-01-24
 */
@Data
public class DockerStatsReq implements Serializable {
    private LocalDateTime statDatetime = LocalDateTime.now();
    @JsonProperty("Name")
    private String name;
    @JsonProperty("BlockIO")
    private String blockIo;
    @JsonProperty("NetIO")
    private String netIo;
    @JsonProperty("CPUPerc")
    private String cpuPerc;
    @JsonProperty("Container")
    private String containerId;
    @JsonProperty("MemPerc")
    private String memPerc;
    @JsonProperty("MemUsage")
    private String memUsage;

    @JsonIgnore
    public DockerStatsPersist toPersist() {
        DockerStatsPersist p = new DockerStatsPersist();
        p.setStatDatetime(statDatetime);
        p.setName(name);
        p.setBlockIo(blockIo);
        p.setNetIo(netIo);
        p.setCpuPerc(cpuPerc);
        p.setContainerId(containerId);
        p.setMemPerc(memPerc);
        p.setMemUsage(memUsage);
        p.setId(null);
        return p;
    }
}
