package org.cloudbus.cloudsim.brokers;

import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.vms.Vm;

import java.util.Comparator;

/**
 * A implementation of {@link DatacenterBroker} that uses a Best Fit
 * mapping between submitted cloudlets and Vm's, trying to place a Cloudlet
 * at the best suitable Vm which can be found (according to the required Cloudlet's PEs).
 * The Broker then places the submitted Vm's at the first Datacenter found.
 * If there isn't capacity in that one, it will try the other ones.
 * *一个{@link DatacenterBroker}的实现，在提交的Cloudlet和Vm之间使用一个最佳匹配映射，
 * 试图将一个Cloudlet放在可以找到的最合适的Vm上(根据所需的Cloudlet的PEs)。
 * *代理然后将提交的Vm放在找到的第一个数据中心。
 * *如果那个没有容量，它会尝试其他的。
 *
 * @author Humaira Abdul Salam
 * @since CloudSim Plus 4.3.8
 */
public class DatacenterBrokerBestFit extends DatacenterBrokerSimple {

    /**
     * Creates a DatacenterBroker object.
     *
     * @param simulation The CloudSim instance that represents the simulation the Entity is related to
     */
    public DatacenterBrokerBestFit(final CloudSim simulation) {
        super(simulation);
    }

    /**
     * Selects the VM with the lowest number of PEs that is able to run a given Cloudlet.
     * In case the algorithm can't find such a VM, it uses the
     * default DatacenterBroker VM mapper as a fallback.
     * *选择能够运行给定Cloudlet的PEs最少的VM。
     * *如果算法找不到这样的虚拟机，它使用默认的DatacenterBroker虚拟机映射器作为后备。
     *
     * @param cloudlet the Cloudlet to find a VM to run it
     * @return the VM selected for the Cloudlet or {@link Vm#NULL} if no suitable VM was found
     */
    @Override
    public Vm defaultVmMapper(final Cloudlet cloudlet) {
        if (cloudlet.isBoundToVm()) {
            return cloudlet.getVm();
        }

        final Vm mappedVm = getVmCreatedList()
            .stream()
            .filter(vm -> vm.getExpectedFreePesNumber() >= cloudlet.getNumberOfPes())
            .min(Comparator.comparingLong(Vm::getExpectedFreePesNumber))
            .orElse(Vm.NULL);

        if (mappedVm == Vm.NULL) {
            LOGGER.warn("{}: {}: {} (PEs: {}) couldn't be mapped to any suitable VM.",
                getSimulation().clockStr(), getName(), cloudlet, cloudlet.getNumberOfPes());
        } else {
            LOGGER.trace("{}: {}: {} (PEs: {}) mapped to {} (available PEs: {}, tot PEs: {})",
                getSimulation().clockStr(), getName(), cloudlet, cloudlet.getNumberOfPes(), mappedVm,
                mappedVm.getExpectedFreePesNumber(), mappedVm.getFreePesNumber());
        }

        return mappedVm;
    }
}
