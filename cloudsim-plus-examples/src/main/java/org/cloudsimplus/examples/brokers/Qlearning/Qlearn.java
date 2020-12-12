package org.cloudsimplus.examples.brokers.Qlearning;
import org.cloudbus.cloudsim.brokers.DatacenterBrokerBestFit;
import org.cloudbus.cloudsim.brokers.DatacenterBrokerQlearn;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.cloudlets.CloudletSimple;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.datacenters.DatacenterQlearn;
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModel;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelFull;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.vms.VmSimple;
import org.cloudsimplus.builders.tables.CloudletsTableBuilder;
import org.cloudsimplus.examples.ReadCsv.ReadCsv;
import org.cloudsimplus.examples.brokers.CloudletToVmMappingBestFit;
import org.cloudsimplus.heuristics.CloudletToVmMappingQlearn;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Arrays;

public class Qlearn{
    private static final int HOSTS = 30;
    private static final int HOST_PES = 8;

    private static final int VMS = 4;

    private static final int CLOUDLETS = readdata[0];//读取data中0列的数据，即数据中心0的数据
    private static final int CLOUDLET_LENGTH = 10000;


    private final CloudSim simulation;
    private DatacenterBrokerQlearn broker0;
    private List<Vm> vmList;
    private List<Cloudlet> cloudletList;
    //todo: DatacenterQlearn
    private DatacenterQlearn datacenter0;
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
        broker0 = new DatacenterBrokerBestFit(simulation);

        vmList = createVms();
        cloudletList = createCloudlets();
        broker0.submitVmList(vmList);
        broker0.submitCloudletList(cloudletList);

        simulation.start();

        final List<Cloudlet> finishedCloudlets = broker0.getCloudletFinishedList();
        finishedCloudlets.sort(Comparator.comparingLong(Cloudlet::getId));
        new CloudletsTableBuilder(finishedCloudlets).build();
    }





    /**
     * 创建cloudlet，从data中读取出来的300s内的总量
     */
    private List<Cloudlet> createCloudlets() {
        final List<Cloudlet> list = new ArrayList<>(CLOUDLETS);
        UtilizationModel utilization = new UtilizationModelFull();
        for (int i = 0; i < CLOUDLETS; i++) {
            Cloudlet cloudlet =
                new CloudletSimple(CLOUDLET_LENGTH, i+1)
                    .setFileSize(1024)
                    .setOutputSize(1024)
                    .setUtilizationModel(utilization);
            list.add(cloudlet);
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
        for (int i = VMS-1; i >= 0; i--) {
            Vm vm =
                new VmSimple(1000, i+1)
                    .setRam(512).setBw(1000).setSize(10000)
                    .setCloudletScheduler(new CloudletSchedulerTimeShared());

            list.add(vm);
        }

        return list;
    }

}

    /**
     * 从data.csv中读取某个数据中心(列号)的负载时间序列
     * 每一行代表300s的采样间隔时间
     *
     */
    private ReadCsv(int row,int column){

    }



    /**
     * Creates a Cloudlet with the given information and adds to the list of {@link #jobs}.
     *
     * @param id a Cloudlet ID
     * @param submitTime Cloudlet's submit time
     * @param runTime The number of seconds the Cloudlet has to run. Considering that
     * and the {@link #rating}, the {@link Cloudlet#cloudletLength} is computed.
     * @param numProc number of Cloudlet's PEs
     * @param reqRunTime user estimated run time
     * (@todo the parameter is not being used and it is not clear what it is)
     * @param userID user id
     * @param groupID user's group id
     * @pre id >= 0
     * @pre submitTime >= 0
     * @pre runTime >= 0
     * @pre numProc > 0
     * @post $none
     * @see #rating
     */
    private void createJob(
        final int id,
        final long submitTime,
        final int runTime,
        final int numProc,
        final int reqRunTime,
        final int userID,
        final int groupID) {
        // create the cloudlet
        final int len = runTime * rating;
        UtilizationModel utilizationModel = new UtilizationModelFull();
        final Cloudlet wgl = new Cloudlet(
            id,
            len,
            numProc,
            0,
            0,
            utilizationModel,
            utilizationModel,
            utilizationModel);
        jobs.add(wgl);
    }

/**
 * 创建cloudlet的函数
 *
 */
        //step.1 读取数据中心0的全部负载序列，并转换integer到int数组
        ReadCsv readCsv=new ReadCsv(0);
        //随着300s间隔读取第i行时刻的总负载
        //todo 系统间隔时间
        if time/系统间隔时间==0 && 最近一次时间更新
            readCsv.getJob(i++);
}
