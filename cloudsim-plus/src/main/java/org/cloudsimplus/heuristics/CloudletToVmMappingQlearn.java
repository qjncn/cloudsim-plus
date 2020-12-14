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
package org.cloudsimplus.heuristics;

import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.distributions.ContinuousDistribution;
import org.cloudbus.cloudsim.vms.Vm;

import java.util.ArrayList;
import java.util.Arrays;
//import java.util.List;
import com.sun.tools.javac.util.List;

/**
 *实现 Qlearn算法，返回
 *
 */
public class CloudletToVmMappingQlearn
{

    /** @see #getVmList() */
    private List<Vm> vmList;

    /** @see #getCloudletList() */
    private List<Cloudlet> cloudletList;

    private int NumOfCloudlet;
    private int NumOfVM;
    private double[][] Q;
    private double[][] graph;
    private double epsilon = 0.8;
    private double alpha = 0.2;
    private double gamma = 0.8;

    public  CloudletToVmMappingQlearn(List<Cloudlet> cloudletList,List<Vm> vmList) {
        this.cloudletList = cloudletList;
        this.vmList = vmList;
        this.NumOfCloudlet = cloudletList.size();
        this.NumOfVM = vmList.size();

        /**
         * 状态空间由C和V组成，C是300秒内所有cloudlet的有序集合，V是300秒内所有VM的有序集合
         *
         */
        /**
         * 构造Q矩阵，默认是0
         */

        this.Q = new double[NumOfCloudlet][NumOfVM];

        /**
         * reward图 先考虑时延的负数（或倒数）
         * 时延 t定义包括：job产生时刻到处理完之间的时间，其中包括了的处理时间和队列等待时间。
         * 这里为了突出算法，只考虑处理时间。
         * 所以一个任务的时延 t = len(cloudlet)/mips(VM)
         * graph[i][j] 表示将第i个cloudlet分配给第j个VM的 时延矩阵
         *
         */
        this.graph = new double[NumOfCloudlet][NumOfVM];
        for (int i = 0; i < NumOfCloudlet; i++) {
            for (int j = 0; j < NumOfVM; j++) {
                graph[i][j] = cloudletList[i].getLength() / vmList[j].getMIPS();
            }
        }


        /**
         * 设置超参数
         * @param epsilon： 贪婪因子，概率选择 Q规则，或者随机选择动作，防止局部最优？
         * @param alpha：学习率
         * @param gamma：衰减因子
         * @param MAX_EPISODES：最大迭代次数
         */
        //epsilon = 0.8;
        //alpha = 0.2;
        //gamma = 0.8;
        int MAX_EPISODES = 400; // 一般都通过设置最大迭代次数来控制训练轮数
        for (int episode = 0; episode < MAX_EPISODES; ++episode) {
            System.out.println("第" + episode + "轮训练...");

            List<int> chosenVmID = new ArrayList<int>();
            for (int CloudID = 0; CloudID < NumOfCloudlet; CloudID++) { // 到达目标状态，结束循环，进行下一轮训练
                int VmID;
                //保证候选的VmID 不属于 已经被选过的chosenVmID
                if (Math.random() < epsilon) VmID = max(Q[CloudID], chosenVmID); // 通过 Q 表选择动作，即选出Q表中CloudID行中的最大值，返回列号
                else VmID = randomNext(Q[CloudID]); // 随机选择可行动作
                // 更新排除列表状态
                chosenVmID.add(VmID);
                //todo：奖励函数需要设计，倒数太简单，负数不对，负数上面代码VmID不能用max函数选，应该用min
                double reward = 1/graph[CloudID][VmID]; // 奖励，是延时的倒数
                //更新Q表
                Q[CloudID][VmID] = (1 - alpha) * Q[CloudID][VmID] + alpha * (reward + gamma * maxNextQ(Q[CloudID + 1], chosenVmID));

            }
            System.out.println(Arrays.deepToString(Q));
        }
    }

    /**
     * 蓄水池抽样，等概率选择流式数据
     * @param is Q表中 ‘CloudID’ 所在行向量Q[CloudID]
     * @return 随机的列号
     */
    private static int randomNext(double[] is) { //
        int columns  = 0, n = 1;
        for(int i = 0; i < is.length; ++i) {
            if(is[i] >= 0 && Math.random() < 1.0/n++) columns  = i;
        }
        return columns ;
    }

    /**
     * 找出is向量的最大值，并保证候选的 VmID 不属于 已经被选过的 chosenVmID
     * @param is Q表中 ‘CloudID’ 所在行向量 Q[CloudID]
     * @param chosenVmID 已经被选过的 VM列表，无法再被选，用序号表示，int型，范围是 0到 (NumOfVM-1)
     * @return 该行最大的 Q值对应的列号，即 VmID
     */
    private static int max(double[] is,List<int> chosenVmID) {
        int max = 0;
        for(int i = 1; i < is.length; ++i) {
            //排除已经被选过的VmID
            if(chosenVmID.contains(i)) continue;
            else if(is[i] > is[max]) max = i;
        }
        return max;
    }

    /**
     * 找出is向量的最大值，写法有待改进
     * @param is Q表中 ‘CloudID’ 所在行向量Q[CloudID]
     * @return Q值
     */
    private static double maxNextQ(double[] is,List<int> chosenVmID,int CloudID ) {
        int VmID = max(is,chosenVmID);//下一个动作的最大Q值的VmID
        return is[VmID];
    }



    public List<Vm> getVmList() {
        return vmList;
    }


    public void setVmList(List<Vm> vmList) {
        this.vmList = vmList;
    }


    public List<Cloudlet> getCloudletList() {
        return cloudletList;
    }


    public void setCloudletList(final List<Cloudlet> cloudletList) {
        this.cloudletList = cloudletList;
    }

    /**
     *
     * @param cloudlet
     * @return 返回Q算法中对应的最大Q值的列序号，就是和该行cloudlet匹配的Vm
     *
     */
    public Vm getMappedVm(Cloudlet cloudlet) {
        int cloudId=cloudlet.getId();
        int max = 0;
        for(int i = 0; i < Q[cloudId].length; ++i) {
            if(Q[cloudId][i] > Q[cloudId][max]) max = i;
        }

        return vmList.get(max);
    }

}
