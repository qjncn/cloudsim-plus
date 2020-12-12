/*
 * CloudSim Plus: A modern, highly-extensible and easier-to-use Framework for
 * Modeling and Simulation of Cloud Computing Infrastructures and Services.
 * http://cloudsimplus.org
 *
 *     Copyright (C) 2015-2018 Universidade da Beira Interior (UBI, Portugal) and
 *     the Instituto Federal de Educação Ciência e Tecnologia do Tocantins (IFTO, Brazil).
 *
 *     This file is part of CloudSim Plus.
 *
 *     CloudSim Plus is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     CloudSim Plus is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with CloudSim Plus. If not, see <http://www.gnu.org/licenses/>.
 */
package org.cloudsimplus.examples.power;

import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicy;
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicyBestFit;
import org.cloudbus.cloudsim.brokers.DatacenterBroker;
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.cloudlets.CloudletSimple;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.hosts.HostSimple;
import org.cloudbus.cloudsim.resources.Pe;
import org.cloudbus.cloudsim.resources.PeSimple;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModel;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelDynamic;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelFull;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.vms.VmSimple;
import org.cloudsimplus.builders.tables.CloudletsTableBuilder;
import org.cloudsimplus.listeners.EventInfo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

/**
 * An example showing how to create a Datacenter with all Hosts powered off
 * and let the {@link VmAllocationPolicy} to power Hosts on and off according to demand.
 * For this simulation, the {@link VmAllocationPolicyBestFit} is used.</p>全部关机初始的数据中心，根据VmAllocationPolicy策略让主机按需开关。本例用的是VmAllocationPolicyBestFitv策略
 *
 * <p>The dynamic demand for Hosts is simulated
 * by creating VMs during simulation runtime.
 * To enable this, it's required to define
 * a {@link Datacenter#setSchedulingInterval(double)} scheduling interval}.
 * It's also used a Event Listener to track the simulation clock.
 * This way, when the clock increases according to the scheduling interval,
 * a new VM is created, up to the total VMs defined in {@link #MAX_VMS}.</p> 动态创建VM，模拟时间监视器，负责全局时间
 *
 * <p>It creates one Cloudlet for each VM and the Cloudlets are created with different lengths. 一个VM创建一个cloudlet，并且长度不同
 * This enables VMs to become idle in different times.
 * To ensure idle VMs are destroyed as soon as they become idle                                 VM在空闲时候就销毁，以便我们发现host中的vm数量变化
 * (so that we can quickly check that the number of VMs into a Host has reduced),
 * a {@link DatacenterBroker#setVmDestructionDelayFunction(Function)} is
 * set. Check the {@link #createBroker()} method for details.
 *
 * <p>Since the total number of PEs requested by all VMs is equal to 24
 * and each Host has 8 PEs, it's only used 3 Hosts to meet the demand.</p> 为什么？
 *
 * @author Manoel Campos da Silva Filho
 * @since CloudSim Plus 4.2.0
 */
public class HostActivationExample {
    /**
     * Defines, between other things, the time intervals
     * to keep Hosts CPU utilization history records.
     */
    private static final int SCHEDULING_INTERVAL = 2;   //2秒

    private static final int HOSTS = 5;                 //上面不是说用3个？
    private static final int HOST_PES = 8;              //

    private static final int MAX_VMS = 6;               //200
    private static final int VM_PES = 4;                //需要修改成1
    private static final int VM_MIPS = 1000;

    private static final int CLOUDLET_LENGTH = 20000;

    private final CloudSim simulation;
    private DatacenterBroker broker0;
    private List<Vm> vmList;
    private List<Cloudlet> cloudletList;
    private Datacenter datacenter0;
    private double lastClockTime;
    private long currentActiveHosts;

    public static void main(String[] args) {
        new HostActivationExample();
    }

    private HostActivationExample() {
        /*Enables just some level of log messages.
          Make sure to import org.cloudsimplus.util.Log;*/
        //Log.setLevel(ch.qos.logback.classic.Level.WARN);

        simulation = new CloudSim();
        datacenter0 = createDatacenter();
        broker0 = createBroker();

        vmList = new ArrayList<>(MAX_VMS);
        cloudletList = new ArrayList<>(MAX_VMS);
        createAndSubmitVmsAndCloudlets(1);//creat一个初始的vm和cloudlet分别加到全局vmlist和cloudletlist，并且注册到broker0
        simulation.addOnClockTickListener(this::clockTickListener);//这步骤创建了一个VM

        simulation.start();//sim开始，处理全部event

        final List<Cloudlet> finishedCloudlets = broker0.getCloudletFinishedList();
        finishedCloudlets.sort(Comparator.comparingLong(Cloudlet::getLength).reversed());
        new CloudletsTableBuilder(finishedCloudlets).build();
        printHostsUpTime();//打印出每个主机在线时间
    }

    private void printHostsUpTime() {
        System.out.printf("%nHosts' up time%n");
        for (Host host : datacenter0.getHostList()) {
            System.out.printf("\tHost %4d Total up time: %15.0f seconds%n", host.getId(), host.getTotalUpTime());
        }
    }

    private DatacenterBroker createBroker() {
        final DatacenterBrokerSimple broker = new DatacenterBrokerSimple(simulation);

        /*Indicates that idle VMs must be destroyed right away (0 delay).
        * 指示必须立即销毁空闲vm(0延迟)。这迫使主机也变为空闲
        * This forces the Host to become idle*/
        broker.setVmDestructionDelay(0.0);
        return broker;
    }

    /**
     * Event listener which is called every time the simulation clock advances.
     * Then, if the time defined in the {@link #SCHEDULING_INTERVAL} has passed,
     * it created another VM.
     *此函数取出evnet的时间，发生时候的动作是创造了一个vm，然后更新全局时间。
     * @param info information about the event happened. 事件发生的信息
     */
    private void clockTickListener(final EventInfo info) {
        final double time = Math.floor(info.getTime());     //取得事件发生的时间
        //这三行就是主要的创建VM和cloudlet的代码
        //time>lastClockTime说明事件发生时间time在上次时钟时间之后，正在准备发生，条件具备
        //time % SCHEDULING_INTERVAL == 0说明事件发生时间符合计划间隔时间，补充：这是在事件定义的时候预定的，协同好了计划间隔时间
        if(time > lastClockTime && time % SCHEDULING_INTERVAL == 0) {
            if(vmList.size() < MAX_VMS) {
                createAndSubmitVmsAndCloudlets(1);  //应该是按照2s创建一个vm的意思？？
            }
            printHostsStatistics();//打印出host状态小表格
        }
        lastClockTime = time;//事件已经在上面发生了，所以lastClockTime 时间要更新成事件发生的时间
    }

    //打印出host状态小表格
    private void printHostsStatistics() {
        currentActiveHosts =
            datacenter0
                .getHostList()
                .stream()
                .filter(Host::isActive)
                .count();

        System.out.printf("# %.2f: %d Active Host(s):%n", simulation.clock(), currentActiveHosts);
        datacenter0
            .getHostList()
            .forEach(host -> System.out.printf("\tHost %3d | VMs: %4d | Active: %s %n", host.getId(), host.getVmList().size(), host.isActive()));
        System.out.println();
    }

    private void createAndSubmitVmsAndCloudlets(final int vmNumber) {
        final List<Vm> newVmList = new ArrayList<>(vmNumber);
        final List<Cloudlet> newCloudletList = new ArrayList<>(vmNumber);

        for (int i = 0; i < vmNumber; i++) {
            final Vm vm = createVm();
            final Cloudlet cloudlet = createCloudlet(vm);
            vmList.add(vm);
            newVmList.add(vm);

            cloudletList.add(cloudlet);
            newCloudletList.add(cloudlet);
        }

        broker0.submitVmList(newVmList);
        broker0.submitCloudletList(newCloudletList);
    }

    private Datacenter createDatacenter() {
        final List<Host> hostList = new ArrayList<>(HOSTS);
        for(int i = 0; i < HOSTS; i++) {
            Host host = createHost();
            hostList.add(host);
        }

        final DatacenterSimple dc = new DatacenterSimple(simulation, hostList, new VmAllocationPolicyBestFit());
        dc.setSchedulingInterval(SCHEDULING_INTERVAL);
        return dc;
    }

    /**
     * Creates a Host and doesn't power it on (it will be powered on according to demand).
     * @return a new powered-off Host
     */
    private Host createHost() {
        final List<Pe> peList = new ArrayList<>(HOST_PES);
        //List of Host's CPUs (Processing Elements, PEs)
        for (int i = 0; i < HOST_PES; i++) {
            peList.add(new PeSimple(1000));
        }

        //Indicates if the Host will be powered on or not after creation
        final boolean activateHost = false;

        //The deadline (in seconds) after the Host becoming idle that it will be shutdown
        final int shutdownDeadlineSeconds = 1;//超时阈值timeout

        final Host host = new HostSimple(peList, activateHost);
        host.setIdleShutdownDeadline(shutdownDeadlineSeconds);
        return host;
    }

    /**
     * Creates one VM
     * @return
     */
    private Vm createVm() {
        return new VmSimple(VM_MIPS, VM_PES);
    }

    /**
     * Creates a Cloudlet
     * @param vm the VM to run the Cloudlet
     * @return
     */
    private Cloudlet createCloudlet(Vm vm) {
        final UtilizationModel utilizationModel = new UtilizationModelDynamic(0.4); //设置初始利用率？
        /* Reduces the length to let the firstly created Cloudlets to run longer,
         * ensuring that a positive length is generated (Cloudlets
         * with negative length run indefinitely until being explicitly stopped.
         * 减少长度，让第一个创建的cloudlet运行得更久，确保生成正长度(具有负长度的cloudlet无限期运行，直到显式地停止
         */

        final long len = Math.abs(CLOUDLET_LENGTH - cloudletList.size()*3000);//长度做了变化，需要修改，改成0.5-1.5之间的随机数也可以
        final Cloudlet cloudlet = new CloudletSimple(len, VM_PES);
        cloudlet.setUtilizationModelCpu(new UtilizationModelFull());
        cloudlet.setUtilizationModelRam(utilizationModel);
        cloudlet.setVm(vm);
        return cloudlet;
    }
}
