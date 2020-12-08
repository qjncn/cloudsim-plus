package main;
import org.cloudbus.cloudsim.vms.Vm;
import java.util.List;
import java.util.Arrays;

public class Qlearn{
    //main
    public static void main(String[] args) {
        /**
         * 状态空间由C和V组成，C是cloudlet的类型和数量，V是VM的类型和数量
         *
         */
        int NumOfCloudlet;//即刻到达的一批cloudlet数量，从data读取
        //todo
        int NumOfVM;
        //todo

        /**
         * 构造Q矩阵，默认是0
         */
        double[][] Q = new double[NumOfCloudlet][NumOfVM];

        /**
         * reward图 先考虑时延的负数（或倒数）
         * 时延t定义包括：job产生时刻到处理完之间的时间，其中包括了的处理时间和队列等待时间。
         * 这里为了突出算法，只考虑处理时间。
         * 所以一个任务的时延 t = len(cloudlet)/mips(VM)
         * graph[i][j] 表示将第i个cloudlet分配给第j个VM
         */
        double[][] graph = new double[NumOfCloudlet][NumOfVM];
        for (int i = 0; i < NumOfCloudlet; i++) {
            for (int j = 0; j < NumOfVM; j++) {
                graph[i][j] = CloudletList[i].getLength() / VmList[j].getMIPS();
            }
        }


        /**
         * 设置超参数
         * @param epsilon： 贪婪因子，概率选择Q规则，或者随机选择动作，防止局部最优？
         * @param alpha：学习率
         * @param gamma：衰减因子
         * @param MAX_EPISODES：最大迭代次数
         */
        double epsilon = 0.8;
        double alpha = 0.2;
        double gamma = 0.8;
        int MAX_EPISODES = 400; // 一般都通过设置最大迭代次数来控制训练轮数
        for(int episode = 0; episode < MAX_EPISODES; ++episode) {
            System.out.println("第"+episode+"轮训练...");

            List<int> chosenVmID = new ArrayList<int>();
            while(int CloudID=0,CloudID<NumOfCloudlet,CloudID++) { // 到达目标状态，结束循环，进行下一轮训练
                int VmID;
                //保证候选的VmID 不属于 已经被选过的chosenVmID
                if(Math.random() < epsilon) VmID = max(Q[CloudID],chosenVmID); // 通过 Q 表选择动作，即选出Q表中CloudID行中的最大值，返回列号
                else VmID = randomNext(Q[CloudID]); // 随机选择可行动作
                // 更新排除列表状态
                chosenVmID.add(VmID);
                int reward = - graph[CloudID][VmID]; // 奖励，是延时的负数
                //更新Q表
                // 通过 Q 表选择动作，即选出Q表中next行中的最大值，返回的是reward
                Q[CloudID][VmID] = (1-alpha)*Q[CloudID][VmID] + alpha*(reward+gamma*maxNextQ(Q[CloudID+1]),chosenVmID);

            }
        }
        System.out.println(Arrays.deepToString(Q));
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
     * 找出is向量的最大值，并保证候选的VmID 不属于 已经被选过的chosenVmID
     * @param is Q表中 ‘CloudID’ 所在行向量Q[CloudID]
     * @param chosenVmID 已经被选过的VM列表，无法再被选，用序号表示，int型，范围是0到(NumOfVM-1)
     * @return 该行最大的Q值对应的列号，即VmID
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
     * @return Q值，奖励值
     */
    private static double maxNextQ(double[] is,List<int> chosenVmID) {
        double max = -99999;
        for(int i = 0; i < is.length; ++i) {
            //排除已经被选过的VmID
            if(chosenVmID.contains(i)) continue;
            else if(is[i] > max) max = is[i];
        }
        return max;
    }

}
