package org.cloudsimplus.examples;

import ch.qos.logback.classic.Level;
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.brokers.DatacenterBroker;
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.cloudlets.CloudletSimple;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.datacenters.DatacenterSimple;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.hosts.HostSimple;
import org.cloudbus.cloudsim.resources.Pe;
import org.cloudbus.cloudsim.resources.PeSimple;
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerSpaceShared;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelDynamic;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelFull;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.vms.VmSimple;
import org.cloudsimplus.builders.tables.CloudletsTableBuilder;
import org.cloudsimplus.builders.tables.TextTableColumn;
import org.cloudsimplus.util.Log;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

/**
 *
 *
 * @author Manoel Campos da Silva Filho
 */
public final class CloudsimPlusAllocationIssue {
    private static final int HOSTS = 2;
    private static final int VMS = 5;

    private static final int HOST_PES = 10;
    private static final int HOST_MIPS = 1_000;

    private static final int VM_PES = 4;

    private static final int VM_RAM = 1000;
    private static final int VM_MIPS = 1000;
    private static final int VM_BW = 1000;
    private static final int VM_SIZE = 1000;

    private static final int HOST_RAM = VMS*VM_RAM;
    private static final int HOST_BW = VMS*VM_BW;
    private static final int HOST_STORAGE = VMS*VM_SIZE;

    private static final int CLOUDLET_PES = VM_PES;
    private static final int CLOUDLET_LENGTH = 10_000;

    private CloudSim simulation;
    private DatacenterBroker broker0;
    private List<Vm> vmList;
    private List<Cloudlet> cloudletList;
    private Datacenter datacenter0;

    public static void main(String[] args) {
        new CloudsimPlusAllocationIssue();
    }

    private  CloudsimPlusAllocationIssue() {
        Log.setLevel(Level.WARN);

        simulation = new CloudSim();
        datacenter0 = createDatacenter();

        broker0 = new DatacenterBrokerSimple(simulation);

        vmList = createVms();
        cloudletList = createCloudlets();
        broker0.submitVmList(vmList);
        broker0.submitCloudletList(cloudletList);

        simulation.start();

        cloudletList.sort(Comparator.comparingLong(Cloudlet::getId));
        new CloudletsTableBuilder(cloudletList)
            .setTitle("Submitted Cloudlets")
            .column(1, col -> col.setTitle("Status                       "))
            .removeColumn(2)
            .addColumn(5, new TextTableColumn("VM Status"), cl -> cl.getVm().isFailed() ? "Fail" : "Success")
            .build();
        System.out.printf("Cloudlets  => Submitted: %d Created: %d Finished: %s Waiting: %d%n",
            broker0.getCloudletSubmittedList().size(),
            broker0.getCloudletCreatedList().size(),
            broker0.getCloudletFinishedList().size(),
            broker0.getCloudletWaitingList().size());
        System.out.println("CloudSim Plus: " + CloudSim.VERSION);
        System.out.println();
    }

    private long getVmId(Cloudlet c) {
        return c.getVm().getId();
    }

    private long getHostId(Cloudlet c) {
        return c.getVm().getHost().getId();
    }

    private Host createHost() {
        final List<Pe> peList = IntStream.range(0, HOST_PES).mapToObj(i -> new PeSimple(HOST_MIPS)).collect(toList());

        //Uses ResourceProvisionerSimple by default for RAM and BW.
        return new HostSimple(HOST_RAM, HOST_BW, HOST_STORAGE, peList)
            .setVmScheduler(new VmSchedulerSpaceShared());
    }

    private Datacenter createDatacenter() {
        final List<Host> hostList = new ArrayList<>(HOSTS);
        for(int i = 0; i < HOSTS; i++) {
            hostList.add(createHost());
        }

        return new DatacenterSimple(simulation, hostList, new VmAllocationPolicySimple());
    }

    private List<Vm> createVms() {
        final List<Vm> list = new ArrayList<>(VMS);
        for (int i = 0; i < VMS; i++) {
            //Uses a CloudletSchedulerTimeShared by default to schedule Cloudlets
            final Vm vm = new VmSimple(VM_MIPS, VM_PES);
            vm.setRam(VM_RAM).setBw(VM_BW).setSize(VM_SIZE);
            list.add(vm);
        }

        return list;
    }

    private List<Cloudlet> createCloudlets() {
        final List<Cloudlet> list = new ArrayList<>(VMS);

        final UtilizationModelDynamic utilizationModel = new UtilizationModelDynamic(0.5);

        for (final Vm vm : vmList) {
            final Cloudlet cloudlet = new CloudletSimple(CLOUDLET_LENGTH, CLOUDLET_PES, utilizationModel);
            cloudlet.setUtilizationModelCpu(new UtilizationModelFull());
            cloudlet.setVm(vm);
            list.add(cloudlet);
        }

        return list;
    }
}
