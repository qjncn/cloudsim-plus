package org.cloudsimplus.examples.brokers.Qlearning;
import ch.qos.logback.classic.Level;
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicyFirstFit;
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicyWorstFit;
import org.cloudbus.cloudsim.brokers.DatacenterBroker;
import org.cloudbus.cloudsim.brokers.DatacenterBrokerQlearn;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.cloudlets.CloudletSimple;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.datacenters.DatacenterSimple;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.hosts.HostSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.ResourceProvisionerSimple;
import org.cloudbus.cloudsim.resources.Pe;
import org.cloudbus.cloudsim.resources.PeSimple;
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerSpaceShared;
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModel;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelFull;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.vms.VmSimple;
import org.cloudsimplus.builders.tables.CloudletsTableBuilder;
import org.cloudsimplus.examples.brokers.Qlearning.ReadCsv;
import org.cloudsimplus.heuristics.CloudletToVmMappingQlearn;
import org.cloudsimplus.listeners.EventInfo;
import org.cloudsimplus.util.Log;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Arrays;

public class Qlearn {
    private static final int SCHEDULING_INTERVAL = 300;
    private static final int HOSTS = 30;
    private static final int HOST_PES = 8;

    private int CLOUDLETS = 0;//读取data中某列的数据，在某300s内的总数
    private static final int CLOUDLET_LENGTH = 15000;
    private  int VMS =0;


    private final CloudSim simulation;
    private static List<DatacenterBrokerQlearn> brokers= new ArrayList<>();
    private List<Vm> vmList;
    private List<Cloudlet> cloudletList;
    private List<Cloudlet> totalCloudletList = new ArrayList<>();
    //todo: DatacenterQlearn
    private DatacenterSimple datacenter0;

    private double batchMeanTime;

    //main
    public static void main(String[] args) {
        new Qlearn();
    }

    private Qlearn() {
        Log.setLevel(Level.ALL);

        /*Enables just some level of log messages.
          Make sure to import org.cloudsimplus.util.Log;*/
        //Log.setLevel(ch.qos.logback.classic.Level.WARN);
        cloudletList = new ArrayList<>(CLOUDLETS);//一批的列表,可以累加
        vmList = new ArrayList<>(VMS);

        simulation = new CloudSim();
        simulation.addOnClockTickListener(this::onClockTickListener);
        datacenter0 =  createDatacenter();

       //todo 如何实现 Qlearn算法 Qlearn:CloudletToVmMappingQlearn
        //simulation.addOnClockTickListener(this::createAndSubmitCloudletsAndVmsAndBorkers);
        totalCloudletList = createAndSubmitCloudletsAndVmsAndBorkers();

        simulation.start();
        for (DatacenterBrokerQlearn broker:brokers) {
            final List<Cloudlet> finishedCloudlets = broker.getCloudletFinishedList();
            finishedCloudlets.sort(Comparator.comparingLong(Cloudlet::getId));
            new CloudletsTableBuilder(finishedCloudlets).build();
            System.out.printf(String.valueOf(broker.meanTime));
        }
    }

    /**
     * Shows updates every time the simulation clock advances.
     * @param evt information about the event happened (that for this Listener is just the simulation time)
     */
    private void onClockTickListener(EventInfo evt) {
        vmList.forEach(vm ->
            System.out.printf(
                "\t\tTime %6.1f: Vm %d CPU Usage: %6.2f%% (%2d vCPUs. Running Cloudlets: #%d). RAM usage: %.2f%% (%d MB)%n",
                evt.getTime(), vm.getId(), vm.getCpuPercentUtilization()*100.0, vm.getNumberOfPes(),
                vm.getCloudletScheduler().getCloudletExecList().size(),
                vm.getRam().getPercentUtilization()*100, vm.getRam().getAllocatedResource())
        );
    }


    /**
     * 创建cloudlet，从data中读取出总数分批次设置延迟，构造
     */
    private List<Cloudlet> createAndSubmitCloudletsAndVmsAndBorkers() {

        ReadCsv readCsv = new ReadCsv(0);
        //读取到全部负载数组
        int[] totalNumCLOUDLETS = readCsv.getCloudletList();
        //debug 调试用，取前几个值
        totalNumCLOUDLETS = Arrays.copyOfRange(totalNumCLOUDLETS, 0, 1);
        //设置批次延迟
        int n=0;//批次
        //for循环负责批次循环
        for (int e:totalNumCLOUDLETS) {
            int submissionDelay = 300*n;//延迟
            CLOUDLETS = e;//得到一批的总数
            VMS = (int) (0.5*e);
            final List<Cloudlet> list = new ArrayList<>(CLOUDLETS);
            UtilizationModel utilization = new UtilizationModelFull();
            for (int i = 0; i < CLOUDLETS; i++) {
                Cloudlet cloudlet =
                    new CloudletSimple(i,CLOUDLET_LENGTH+2000*i,1)//CLOUDLET_LENGTH长度不同
                        //.setFileSize(1024)
                        //.setOutputSize(1024)
                        .setUtilizationModel(utilization);
                cloudlet.setSubmissionDelay(submissionDelay);
                cloudletList.add(cloudlet);
            }



            //todo 创建批次对应的Vm

            for (int i = 0; i <VMS; i++) {
                Vm vm =
                    new VmSimple(i,100+10*i, 1)
                        //.setRam(512).setBw(1000).setSize(10000)
                        .setCloudletScheduler(new CloudletSchedulerSpaceShared());
                vmList.add(vm);
            }
            DatacenterBrokerQlearn brokertemp= new DatacenterBrokerQlearn(simulation,cloudletList,vmList);
            brokers.add(brokertemp);
            brokertemp.submitVmList(vmList);
            brokertemp.submitCloudletList(cloudletList);
            batchMeanTime = brokertemp.meanTime;
            System.out.printf("batch %d 's mean time is %f",n,batchMeanTime);
            n++;
            totalCloudletList.addAll(cloudletList);//增加一批到总表
        }
        return totalCloudletList;
    }

    /**
     * Creates a list of VMs with decreasing number of PEs.
     * The IDs of the VMs aren't defined and will be set when
     * they are submitted to the broker.
     */


    private Host createHost(int id) {
        List<Pe> peList = new ArrayList<>();
        long mips = 4000;
        for (int i = 0; i < HOST_PES; i++) {
            peList.add(new PeSimple(mips, new PeProvisionerSimple()));
        }
        long ram = 2048; // host memory (Megabyte)
        long storage = 1000000; // host storage (Megabyte)
        long bw = 10000; //Megabits/s

        return new HostSimple(peList)
            //.setRamProvisioner(new ResourceProvisionerSimple())
            //.setBwProvisioner(new ResourceProvisionerSimple())
            .setVmScheduler(new VmSchedulerSpaceShared());

    }


    private DatacenterSimple createDatacenter() {
        final List<Host> hostList = new ArrayList<>(HOSTS);
        for(int i = 0; i < HOSTS; i++) {
            Host host = createHost(i);
            hostList.add(host);
        }

        //Uses a VmAllocationPolicySimple by default to allocate VMs
        DatacenterSimple dc0 = new DatacenterSimple(simulation, hostList,new VmAllocationPolicyFirstFit());
        dc0.setSchedulingInterval(SCHEDULING_INTERVAL);
        return dc0;
    }
    private void printResults() {
        for (DatacenterBrokerQlearn broker : brokers) {
            new CloudletsTableBuilder(broker.getCloudletFinishedList())
                .setTitle(broker.getName())
                .build();
            System.out.printf(String.valueOf(broker.meanTime));
        }
    }
}
