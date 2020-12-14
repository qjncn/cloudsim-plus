package org.cloudsimplus.examples.brokers.Qlearning;
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.brokers.DatacenterBrokerBestFit;
import org.cloudbus.cloudsim.brokers.DatacenterBrokerQlearn;
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.cloudlets.CloudletSimple;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.datacenters.DatacenterQlearn;
import org.cloudbus.cloudsim.datacenters.DatacenterSimple;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.hosts.HostSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.ResourceProvisionerSimple;
import org.cloudbus.cloudsim.resources.Pe;
import org.cloudbus.cloudsim.resources.PeSimple;
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerSpaceShared;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModel;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelFull;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.vms.VmSimple;
import org.cloudsimplus.builders.tables.CloudletsTableBuilder;
import org.cloudsimplus.examples.brokers.Qlearning.ReadCsv;
import org.cloudsimplus.examples.brokers.CloudletToVmMappingBestFit;
import org.cloudsimplus.heuristics.CloudletToVmMappingQlearn;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Arrays;

public class Qlearn {
    private static final int HOSTS = 30;
    private static final int HOST_PES = 8;

    private static final int VMS = 4;

    private static final int CLOUDLETS = 0;//读取data中某列的数据，在某300s内的总数
    private static final int CLOUDLET_LENGTH = [10000,];


    private final CloudSim simulation;
    private DatacenterBrokerSimple broker0;
    private List<Vm> vmList;
    private List<Cloudlet> cloudletList;
    private List<Cloudlet> totalCloudletList=new List<Cloudlet>();
    //todo: DatacenterQlearn
    private DatacenterSimple datacenter0;

    //main
    public static void main(String[] args) {
        new Qlearn();
    }

    private Qlearn() {

        /*Enables just some level of log messages.
          Make sure to import org.cloudsimplus.util.Log;*/
        //Log.setLevel(ch.qos.logback.classic.Level.WARN);

        simulation = new CloudSim();
        datacenter0 = createDatacenter();

        //Creates a broker that is a software acting on behalf a cloud customer to manage his/her VMs and Cloudlets
        broker0 = new DatacenterBrokerSimple(simulation);

        cloudletList = createCloudlets();
        vmList = createVms();
        broker0.submitVmList(vmList);
        broker0.submitCloudletList(cloudletList);

        simulation.start();

        final List<Cloudlet> finishedCloudlets = broker0.getCloudletFinishedList();
        finishedCloudlets.sort(Comparator.comparingLong(Cloudlet::getId));
        new CloudletsTableBuilder(finishedCloudlets).build();
    }


    /**
     * 创建cloudlet，从data中读取出总数分批次设置延迟，构造
     */
    private List<Cloudlet> createCloudlets() {
        ReadCsv readCsv = new ReadCsv(0);
        //读取到全部负载数组
        int[] totalNumCLOUDLETS = readCsv.getCloudletList();
        //设置批次延迟
        int n=0;//批次
        for (int e:totalNumCLOUDLETS) {
            int submissionDelay=300*n;//延迟
            CLOUDLETS = e;//得到一批的总数
            final List<Cloudlet> list = new ArrayList<>(CLOUDLETS);//一批的列表
            UtilizationModel utilization = new UtilizationModelFull();
            for (int i = 0; i < CLOUDLETS; i++) {
                Cloudlet cloudlet =
                    new CloudletSimple(CLOUDLET_LENGTH,1)//CLOUDLET_LENGTH长度随机
                        .setFileSize(1024)
                        .setOutputSize(1024)
                        .setUtilizationModel(utilization);
                cloudlet.setSubmissionDelay(submissionDelay);
                list.add(cloudlet);
            }
            n++;
            totalCloudletList.addAll(list);
        }


        return list;
    }

    /**
     * Creates a list of VMs with decreasing number of PEs.
     * The IDs of the VMs aren't defined and will be set when
     * they are submitted to the broker.
     */
    private List<Vm> createVms() {
        final List<Vm> list = new ArrayList<>(VMS);
        for (int i = VMS - 1; i >= 0; i--) {
            Vm vm =
                new VmSimple(1000, i + 1)
                    .setRam(512).setBw(1000).setSize(10000)
                    .setCloudletScheduler(new CloudletSchedulerTimeShared());

            list.add(vm);
        }

        return list;
    }

    private Host createHost(int id) {
        List<Pe> peList = new ArrayList<>();
        long mips = 1000;
        for (int i = 0; i < HOST_PES_NUMBER; i++) {
            peList.add(new PeSimple(mips, new PeProvisionerSimple()));
        }
        long ram = 2048; // host memory (Megabyte)
        long storage = 1000000; // host storage (Megabyte)
        long bw = 10000; //Megabits/s

        return new HostSimple(ram, bw, storage, peList)
            .setRamProvisioner(new ResourceProvisionerSimple())
            .setBwProvisioner(new ResourceProvisionerSimple())
            .setVmScheduler(new VmSchedulerSpaceShared());

    }


    /**
     * 从data.csv中读取某个数据中心(列号)的负载时间序列
     * 每一行代表300s的采样间隔时间
     */
    private ReadCsv(int row, int column) {

    }

    private Datacenter createDatacenter() {
        Host host = createHost(0);
        hostList.add(host);
        return new DatacenterSimple(simulation, hostList, new VmAllocationPolicySimple());
    }
}
