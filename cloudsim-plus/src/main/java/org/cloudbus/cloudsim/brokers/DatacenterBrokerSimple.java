/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */
package org.cloudbus.cloudsim.brokers;

import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.vms.Vm;

/**
 * A simple implementation of {@link DatacenterBroker} that try to host customer's VMs
 * at the first Datacenter found. If there isn't capacity in that one,
 * it will try the other ones.
 *一个简单的实现{@link DatacenterBroker}，
 * 它尝试在找到的第一个数据中心托管客户的虚拟机。如果这个没有容量，它会尝试其他的。
 *
 * <p>The default selection of VMs for each cloudlet is based on a Round-Robin policy,
 * cyclically selecting the next VM from the broker VM list for each requesting
 * cloudlet.
 * However, when {@link #setSelectClosestDatacenter(boolean) selection of the closest datacenter}
 * is enabled, the broker will try to place each VM at the closest Datacenter as possible,
 * according to their timezone.</p>
 * <p>每个cloudlet的缺省VM选择是基于循环策略的，循环地从代理VM列表中为每个请求的cloudlet选择下一个VM。
 * *但是，当{@link #setSelectClosestDatacenter(boolean) selection of the closest datacenter}启用时，
 * 代理将根据各自的时区，尽可能将每个虚拟机放置在最近的数据中心
 *
 *
 * <p>Such a policy doesn't check if the selected VM is really suitable for the Cloudlet
 * and may not provide an optimal mapping.</p>
 * 这样的策略不会检查所选的VM是否真的适合Cloudlet，可能不会提供最优映射
 *
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @author Manoel Campos da Silva Filho
 * @since CloudSim Toolkit 1.0
 *
 * @see DatacenterBrokerFirstFit
 * @see DatacenterBrokerBestFit
 * @see DatacenterBrokerHeuristic
 */
public class DatacenterBrokerSimple extends DatacenterBrokerAbstract {
    /**
     * Index of the last VM selected from the {@link #getVmExecList()}
     * to run some Cloudlet.
     * 从{@link #getVmExecList()}中选择运行某个Cloudlet的最后一个VM的索引。
     */
    private int lastSelectedVmIndex;

    /**
     * Index of the last Datacenter selected to place some VM.
     * 选择放置某个VM的最后一个数据中心的索引。
     */
    private int lastSelectedDcIndex;

    /**
     * Creates a new DatacenterBroker.
     *创建一个新的DatacenterBroker。
     * @param simulation the CloudSim instance that represents the simulation the Entity is related to
     */
    public DatacenterBrokerSimple(final CloudSim simulation) {
        this(simulation, "");
    }

    /**
     * Creates a DatacenterBroker giving a specific name.
     *创建提供特定名称的DatacenterBroker。
     *
     * @param simulation the CloudSim instance that represents the simulation the Entity is related to
     * @param name the DatacenterBroker name
     */
    public DatacenterBrokerSimple(final CloudSim simulation, final String name) {
        super(simulation, name);
        this.lastSelectedVmIndex = -1;
        this.lastSelectedDcIndex = -1;
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>It applies a Round-Robin policy to cyclically select
     * the next Datacenter from the list. However, it just moves
     * to the next Datacenter when thecc previous one was not able to create
     * all {@link #getVmWaitingList() waiting VMs}.</p>
     *<p><b>它应用一个循环策略循环地从列表中选择下一个数据中心。
     * 但是，当前一个数据中心无法创建所有{@link #getVmWaitingList() waiting VMs}时，它将移到下一个数据中心
     *
     * <p>This policy is just used if the selection of the closest Datacenter is not enabled.
     * Otherwise, the {@link #closestDatacenterMapper(Datacenter, Vm)} is used instead.</p>
     *<p>该策略仅在未启用 最近数据中心 的情况下使用。
     * 否则，使用{@link #closestDatacenterMapper(Datacenter, Vm)}代替
     *
     * @param lastDatacenter {@inheritDoc}
     * @param vm {@inheritDoc}
     * @return {@inheritDoc}
     * @see DatacenterBroker#setDatacenterMapper(java.util.function.BiFunction)
     * @see #setSelectClosestDatacenter(boolean)
     */
    @Override
    protected Datacenter defaultDatacenterMapper(final Datacenter lastDatacenter, final Vm vm) {
        if(getDatacenterList().isEmpty()) {
            throw new IllegalStateException("You don't have any Datacenter created.");
        }

        if (lastDatacenter != Datacenter.NULL) {
            return getDatacenterList().get(lastSelectedDcIndex);
        }

        /*If all Datacenter were tried already, return Datacenter.NULL to indicate
        * there isn't a suitable Datacenter to place waiting VMs.
        * 如果所有数据中心都已尝试，则返回 Datacenter.NULL 表示没有合适的数据中心来放置等待的虚拟机。*/

        if(lastSelectedDcIndex == getDatacenterList().size()-1){
            return Datacenter.NULL;
        }
        //功能实现部分：找到最后一次选过的DC的下一个位置的DC
        return getDatacenterList().get(++lastSelectedDcIndex);
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>It applies a Round-Robin policy to cyclically select
     * the next Vm from the {@link #getVmWaitingList() list of waiting VMs}.</p>
     * <p><b>它应用一个循环策略从{@link #getVmWaitingList() list of waiting VMs}中循环地选择下一个Vm
     *
     * @param cloudlet {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    protected Vm defaultVmMapper(final Cloudlet cloudlet) {
        if (cloudlet.isBoundToVm()) {
            return cloudlet.getVm();
        }

        if (getVmExecList().isEmpty()) {
            return Vm.NULL;
        }

        /*If the cloudlet isn't bound to a specific VM or the bound VM was not created,
        cyclically selects the next VM on the list of created VMs.
        如果cloudlet没有绑定到特定的VM，或者绑定的VM没有创建，循环地选择已创建VM列表中的下一个VM
        */
        lastSelectedVmIndex = ++lastSelectedVmIndex % getVmExecList().size();
        return getVmFromCreatedList(lastSelectedVmIndex);
    }
}
