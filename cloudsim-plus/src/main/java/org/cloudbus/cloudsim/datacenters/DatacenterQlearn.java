package org.cloudbus.cloudsim.datacenters;



import org.cloudbus.cloudsim.core.Simulation;
import org.cloudbus.cloudsim.core.events.SimEvent;
import org.cloudbus.cloudsim.datacenters.DatacenterSimple;
import org.cloudbus.cloudsim.hosts.Host;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;




/**
 * 本类实现了：如何编码实体要采用的周期性行为？
 * 参考：https://cloudsimplus.readthedocs.io/en/latest/FAQ/
 * 1扩展 DatacenterSimple
 * 2定义一个新标签tag 来描述周期性事件
 * 3覆盖processOtherEvent，以检测定期事件并为其调用处理程序
 * 4实现处理程序方法。最终，此方法还计划该事件的下一个调用。
 */
public class DatacenterQlearn extends DatacenterSimple {
    //choose any unused value you want to represent the tag.
    public static final int PERIODIC_EVENT = 12345;
    Logger LOGGER = LoggerFactory.getLogger(Datacenter.class.getSimpleName());
    public DatacenterQlearn(){}

    @Override
    protected void processOtherEvent(SimEvent ev) {
        if (ev == null){
            LOGGER.debug("Warning: " + getSimulation().clock()+": "+this.getName()+": Null event ignored.");
        } else {
            int tag = ev.getTag();
            switch(tag){
                case PERIODIC_EVENT: processPeriodicEvent(ev); break;
                default:
                    LOGGER.debug("Warning: " + getSimulation().clock() + ":" + this.getName() + ": Unknown event ignored. Tag:" + tag);
            }
        }
    }

    private void processPeriodicEvent(SimEvent ev) {
        //your code here
        //contains the delay to the next periodic event 包含到下一个周期事件的延迟
        float delay;
        //true if new internal events have to be generated如果必须生成新的内部事件，则为真
        boolean generatePeriodicEvent;
        if (generatePeriodicEvent) {
            //java.util.Objects.data;
            //The actual class of this data is defined by the entity that generates the event.
            send(getId(), delay,PERIODIC_EVENT, ev.getData());
        }
    }
}
