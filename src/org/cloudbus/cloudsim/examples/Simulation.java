

package org.cloudbus.cloudsim.examples;
import java.io.File;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.*;


import com.opencsv.CSVWriter;
import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;



/**
 * An example showing how to create
 * scalable simulations.
 */
public class Simulation {

    /** The cloudlet list. */
    private static List<Cloudlet> cloudletList;


    /** The vmlist. */
    private static List<Vm> vmlist;



    private static List<Integer> create(int num,int start, int end)
    {
        LinkedList<Integer> list = new LinkedList<Integer>();
        for(int i=0;i<num;i++) {
            Random r = new Random();
            Integer a = start + r.nextInt(end);
            list.add(a);
        }
        return list;
    }


    private static List<Vm> createVM(int userId, int vms,List<Integer> vmMips) {
        LinkedList<Vm> list = new LinkedList<Vm>();

        //VM Parameters
        long size = 10000; //image size (MB)
        int ram = 512; //vm memory (MB)

        long bw = 1000;
        int pesNumber = 1; //number of cpus
        String vmm = "Xen"; //VMM name

        Vm[] vm = new Vm[vms];


        for(int i=0;i<vms;i++){

            vm[i] = new Vm(i, userId, vmMips.get(i), pesNumber, ram, bw, size, vmm, new CloudletSchedulerSpaceShared());
            //for creating a VM with a space shared scheduling policy for cloudlets:
            //Log.printLine(vm[i].getId()+" -> Mips = "+vm[i].getMips());

            list.add(vm[i]);
        }
        return list;
    }


    private static List<Cloudlet> createCloudlet(int userId, int cloudlets,List<Integer> cloutletLength){
        // Creates a container to store Cloudlets
        LinkedList<Cloudlet> list = new LinkedList<Cloudlet>();

        //cloudlet parameters

        long fileSize = 300;
        long outputSize = 300;
        int pesNumber = 1;
        UtilizationModel utilizationModel = new UtilizationModelFull();

        Cloudlet[] cloudlet = new Cloudlet[cloudlets];

        for(int i=0;i<cloudlets;i++){
            cloudlet[i] = new Cloudlet(i, cloutletLength.get(i), pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
            // setting the owner of these Cloudlets
            cloudlet[i].setUserId(userId);
            list.add(cloudlet[i]);
        }

        return list;
    }


    ////////////////////////// STATIC METHODS ///////////////////////

    /**
     * Creates main() to run this example
     */
    public static void main(String[] args) {
        Log.printLine("Starting CloudSimExample6...");

        try {
            Scanner scanner = new Scanner(System.in);
            int noOfVm,noOfCloutlet;
            Log.printLine("Enter the number of VMs: ");
            noOfVm = scanner.nextInt();
            Log.printLine("Enter the number of Cloudlets: ");
            noOfCloutlet=scanner.nextInt();


            List vmsMips = create(noOfVm,400,600);
            List cloutletLength = create(noOfCloutlet,1000,2000);

            List<Cloudlet> algoList = Algo(noOfVm,noOfCloutlet,vmsMips,cloutletLength);
            List<Cloudlet> roundRobinList = roundRobin(noOfVm,noOfCloutlet,vmsMips,cloutletLength);
            printCloudletList(algoList,"Algo");
            printCloudletList(roundRobinList,"Round Robin");

            Collections.sort(algoList, new Comparator<Cloudlet>() {
                @Override
                public int compare(Cloudlet o1, Cloudlet o2) {
                    return o1.getCloudletId() - o2.getCloudletId();
                }
            });
            Collections.sort(roundRobinList, new Comparator<Cloudlet>() {
                @Override
                public int compare(Cloudlet o1, Cloudlet o2) {
                    return o1.getCloudletId() - o2.getCloudletId();
                }
            });
            String[] data = new String[5];
            data[0] = "Cloutlet-ID";
            data[1] = "Algo Wait Time";
            data[2] = "Round-Robin Wait Time";
            data[3] = "Algo Execution Time";
            data[4] = "Round-Robin Execution Time";
            String csv = "Outputs/Statistics.csv";
            CSVWriter writer = new CSVWriter(new FileWriter(csv, false));
            writer.writeNext(data);
            DecimalFormat dft = new DecimalFormat("###.##");
            double responseTimeAlgo = 0.0;
            double responseTimeRound = 0.0;
            double waitingTimeAlgo = 0.0;
            double waitingTimeRound = 0.0;
            for(int i=0; i < noOfCloutlet; i++)
            {
                data[0] = ""+i;
                data[1] = "" + dft.format(algoList.get(i).getExecStartTime());
                data[2] = "" + dft.format(roundRobinList.get(i).getExecStartTime());
                data[3] = ""+ dft.format(algoList.get(i).getActualCPUTime());
                data[4] = ""+ dft.format(roundRobinList.get(i).getActualCPUTime());
                responseTimeAlgo +=Double.parseDouble(dft.format(algoList.get(i).getExecStartTime()));
                responseTimeRound+=Double.parseDouble(dft.format(roundRobinList.get(i).getExecStartTime()));
                waitingTimeAlgo+=Double.parseDouble(dft.format(algoList.get(i).getActualCPUTime()));
                waitingTimeRound+=Double.parseDouble(dft.format(roundRobinList.get(i).getActualCPUTime()));
                writer.writeNext(data);
            }
            responseTimeAlgo/=noOfCloutlet;
            responseTimeRound/=noOfCloutlet;
            waitingTimeAlgo/=noOfCloutlet;
            waitingTimeRound/=noOfCloutlet;

            Log.printLine("Average Response Time");
            Log.printLine("Algo:- "+responseTimeAlgo+"\t\t\t\tRound-Robin:- "+responseTimeRound);
            Log.printLine("Average Waiting Time");
            Log.printLine("Algo:- "+waitingTimeAlgo+"\t\t\t\tRound-Robin:- "+waitingTimeRound);
            writer.close();


            //graph();


        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.printLine("The simulation has been terminated due to an unexpected error");
        }
    }

//    private static void graph()
//    {
//        XYSeriesCollection r1 = new XYSeriesCollection();
//        XYSeries s1 = new XYSeries("Series 1",false,true);
//        s1.add(1.1,2.0);
//        s1.add(2.1,5.0);
//        XYSeries s2 = new XYSeries("Series 2",false,true);
//        s1.add(1.0, 4.0);
//        s1.add(2.0, 3.0);
//        r1.addSeries(s1);
//        r1.addSeries(s2);
//        JFreeChart chart = ChartFactory.createXYLineChart("XY Chart",
//                "x-axis",
//                "y-axis",
//                r1,
//                PlotOrientation.VERTICAL,
//                true,
//                true,false);
//        try{
//            ChartUtilities.saveChartAsJPEG(new File("chart.JPEG"),chart,500,300);
//        }
//        catch (Exception e)
//        {
//
//        }
//
//    }

    private static void upload(double mips[], double tasks[], double loads[], int vm, int cloudlet, int noOfVm)
    {
        String data[] = new String[noOfVm*3 + 4];
        data[0] = "CL = " + cloudlet;
        data[1] = " -> ";
        data[2] = "VM = " + vm;
        data[3] = "";
        for(int i=0; i<noOfVm; i++)
        {

            data[(i*3)+4] = ""+tasks[i];
            data[(i*3)+5] = ""+loads[i];
            data[(i*3)+6] = "";
        }
        try {
            String csv = "Outputs/SchedulingData.csv";
            CSVWriter writer = new CSVWriter(new FileWriter(csv, true));
            writer.writeNext(data);
            writer.close();
        }
        catch (Exception e)
        {
            Log.printLine("Error occurred -> " + e);
        }
    }
    public static List<Cloudlet> roundRobin(int noOfVM, int noOfCloutlet, List<Integer> vmsMips, List<Integer> cloutletLength)
    {
        try{


            //Third step: Create Broker
            int num_user = 3;   // number of grid users
            Calendar calendar = Calendar.getInstance();
            boolean trace_flag = false;  // mean trace events

            // Initialize the CloudSim library
            CloudSim.init(num_user, calendar, trace_flag);
            Datacenter datacenter0 = createDatacenter("Datacenter_0");
            Datacenter datacenter1 = createDatacenter("Datacenter_1");

            DatacenterBroker broker = createBroker();
            int brokerId = broker.getId();

            //Fourth step: Create VMs and Cloudlets and send them to broker
            vmlist = createVM(brokerId,noOfVM,vmsMips); //creating 20 vms
            cloudletList = createCloudlet(brokerId,noOfCloutlet,cloutletLength); // creating 40 cloudlets

            int j = 0;
            for(int i=0; i<noOfCloutlet; i++)
            {
                cloudletList.get(i).setVmId(j);
                j = (j+1)%noOfVM;
            }


            broker.submitVmList(vmlist);
            broker.submitCloudletList(cloudletList);

            // Fifth step: Starts the simulation
            CloudSim.startSimulation();

            // Final step: Print results when simulation is over
            List<Cloudlet> newList = broker.getCloudletReceivedList();

            CloudSim.stopSimulation();

            //printCloudletList(newList);
            Log.printLine("CloudSimExample6 finished!");
            return newList;

            //Print the debt of each user to each datacenter


        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.printLine("The simulation has been terminated due to an unexpected error");
        }
        return new LinkedList<Cloudlet>();
    }
    public static List<Cloudlet> Algo(int noOfVM, int noOfCloutlet, List<Integer> vmsMips, List<Integer> cloutletLength)
    {
        try{


            //Third step: Create Broker
            int num_user = 3;   // number of grid users
            Calendar calendar = Calendar.getInstance();
            boolean trace_flag = false;  // mean trace events

            // Initialize the CloudSim library
            CloudSim.init(num_user, calendar, trace_flag);
            Datacenter datacenter0 = createDatacenter("Datacenter_0");
            Datacenter datacenter1 = createDatacenter("Datacenter_1");

            DatacenterBroker broker = createBroker();
            int brokerId = broker.getId();

            //Fourth step: Create VMs and Cloudlets and send them to broker
            vmlist = createVM(brokerId,noOfVM,vmsMips); //creating 20 vms
            cloudletList = createCloudlet(brokerId,noOfCloutlet,cloutletLength); // creating 40 cloudlets

            String data[] = new String[noOfVM*3 + 4];
            data[0] = "Cloudlet";
            data[1] = "";
            data[2] = "Vertual Machine";
            data[3] = "";
            for(int i=0; i<noOfVM; i++)
            {
                data[i*3 + 4] = "Length";
                data[i*3+5] = "Load";
                data[i*3+6] = "";
            }
            String csv = "Outputs/SchedulingData.csv";
            CSVWriter writer = new CSVWriter(new FileWriter(csv,false));
            //CSVWriter writer = new CSVWriter(new FileWriter(csv, true));
            writer.writeNext(data);

            data = new String[noOfVM*3 + 4];
            data[0] = "";
            data[1] = "";
            data[2] = "";
            data[3] = "";
            for(int i=0; i<noOfVM; i++)
            {
                data[i*3 + 4] = ""+vmsMips.get(i);
                data[i*3+5] = "";
                data[i*3+6] = "";
            }
            writer.writeNext(data);
            writer.close();


            double mips[] = new double[noOfVM];
            double tasks[] = new double[noOfVM];
            double loads[] = new double[noOfVM];
            for(int i=0; i<noOfVM; i++)
            {
                mips[i] = vmsMips.get(i);
                loads[i] = 0;
                tasks[i] = 0;
            }
            for(int i=0; i<noOfCloutlet;i++)
            {
                int machineId = 0;
                for(int j=1; j<noOfVM; j++)
                {
                    if(loads[machineId] > loads[j])
                    {
                        machineId = j;
                    }
                }
                tasks[machineId] += cloudletList.get(i).getCloudletLength();
                loads[machineId] = tasks[machineId] / mips[machineId];
                (cloudletList.get(i)).setVmId(machineId);
                upload(mips,tasks,loads,machineId,i,noOfVM);
            }




            broker.submitVmList(vmlist);
            broker.submitCloudletList(cloudletList);

            // Fifth step: Starts the simulation
            CloudSim.startSimulation();

            // Final step: Print results when simulation is over
            List<Cloudlet> newList = broker.getCloudletReceivedList();

            CloudSim.stopSimulation();

            //printCloudletList(newList);
            Log.printLine("CloudSimExample6 finished!");
            return newList;

            //Print the debt of each user to each datacenter


        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.printLine("The simulation has been terminated due to an unexpected error");
        }
        return new LinkedList<Cloudlet>();
    }


    private static Datacenter createDatacenter(String name){

        // Here are the steps needed to create a PowerDatacenter:
        // 1. We need to create a list to store one or more
        //    Machines
        List<Host> hostList = new ArrayList<Host>();

        // 2. A Machine contains one or more PEs or CPUs/Cores. Therefore, should
        //    create a list to store these PEs before creating
        //    a Machine.
        List<Pe> peList1 = new ArrayList<Pe>();

        int mips = 1000;

        // 3. Create PEs and add these into the list.
        //for a quad-core machine, a list of 4 PEs is required:
        peList1.add(new Pe(0, new PeProvisionerSimple(mips))); // need to store Pe id and MIPS Rating
        peList1.add(new Pe(1, new PeProvisionerSimple(mips)));
        peList1.add(new Pe(2, new PeProvisionerSimple(mips)));
        peList1.add(new Pe(3, new PeProvisionerSimple(mips)));

        //Another list, for a dual-core machine
        List<Pe> peList2 = new ArrayList<Pe>();

        peList2.add(new Pe(0, new PeProvisionerSimple(mips)));
        peList2.add(new Pe(1, new PeProvisionerSimple(mips)));

        //4. Create Hosts with its id and list of PEs and add them to the list of machines
        int hostId=0;
        int ram = 2048; //host memory (MB)
        long storage = 1000000; //host storage
        int bw = 10000;

        hostList.add(
                new Host(
                        hostId,
                        new RamProvisionerSimple(ram),
                        new BwProvisionerSimple(bw),
                        storage,
                        peList1,
                        new VmSchedulerTimeShared(peList1)
                )
        ); // This is our first machine

        hostId++;

        hostList.add(
                new Host(
                        hostId,
                        new RamProvisionerSimple(ram),
                        new BwProvisionerSimple(bw),
                        storage,
                        peList2,
                        new VmSchedulerTimeShared(peList2)
                )
        ); // Second machine


        //To create a host with a space-shared allocation policy for PEs to VMs:
        //hostList.add(
        //		new Host(
        //			hostId,
        //			new CpuProvisionerSimple(peList1),
        //			new RamProvisionerSimple(ram),
        //			new BwProvisionerSimple(bw),
        //			storage,
        //			new VmSchedulerSpaceShared(peList1)
        //		)
        //	);

        //To create a host with a oportunistic space-shared allocation policy for PEs to VMs:
        //hostList.add(
        //		new Host(
        //			hostId,
        //			new CpuProvisionerSimple(peList1),
        //			new RamProvisionerSimple(ram),
        //			new BwProvisionerSimple(bw),
        //			storage,
        //			new VmSchedulerOportunisticSpaceShared(peList1)
        //		)
        //	);


        // 5. Create a DatacenterCharacteristics object that stores the
        //    properties of a data center: architecture, OS, list of
        //    Machines, allocation policy: time- or space-shared, time zone
        //    and its price (G$/Pe time unit).
        String arch = "x86";      // system architecture
        String os = "Linux";          // operating system
        String vmm = "Xen";
        double time_zone = 10.0;         // time zone this resource located
        double cost = 3.0;              // the cost of using processing in this resource
        double costPerMem = 0.05;		// the cost of using memory in this resource
        double costPerStorage = 0.1;	// the cost of using storage in this resource
        double costPerBw = 0.1;			// the cost of using bw in this resource
        LinkedList<Storage> storageList = new LinkedList<Storage>();	//we are not adding SAN devices by now

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);


        // 6. Finally, we need to create a PowerDatacenter object.
        Datacenter datacenter = null;
        try {
            datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return datacenter;
    }

    //We strongly encourage users to develop their own broker policies, to submit vms and cloudlets according
    //to the specific rules of the simulated scenario
    private static DatacenterBroker createBroker(){


        DatacenterBroker broker = null;
        try {
            broker = new DatacenterBroker("Broker");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return broker;
    }

    /**
     * Prints the Cloudlet objects
     * @param list  list of Cloudlets
     */
    @SuppressWarnings("deprecation")
    private static void printCloudletList(List<Cloudlet> list,String algorithm) {
        int size = list.size();
        Cloudlet cloudlet;
        try {
            String indent = "    ";
            Log.printLine();
            Log.printLine("============================== " + algorithm + " ==============================");
            Log.printLine("Cloudlet ID" + indent + "STATUS" + indent +
                    "Data center ID" + indent + "VM ID" + indent + indent + "Time" + indent + "Start Time" + indent + "Finish Time" + indent + "user id" + indent);


            String data[] = new String[8];
            data[0] = "Cloudlet ID";
            data[1] = "STATUS";
            data[2] = "Data center ID";
            data[3] = "VM ID";
            data[4] = "Time";
            data[5] = "Start Time";
            data[6] = "Finish Time";
            data[7] = "user id";
            String csv = "Outputs/" + algorithm + " Execution Details.csv";
            CSVWriter writer = new CSVWriter(new FileWriter(csv, false));
            writer.writeNext(data);


            DecimalFormat dft = new DecimalFormat("###.##");
            for (int i = 0; i < size; i++) {
                cloudlet = list.get(i);
                Log.print(indent + cloudlet.getCloudletId() + indent + indent);

                if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
                    Log.print("SUCCESS");

                    Log.printLine(indent + indent + cloudlet.getResourceId() + indent + indent + indent + cloudlet.getVmId() +
                            indent + indent + indent + dft.format(cloudlet.getActualCPUTime()) +
                            indent + indent + dft.format(cloudlet.getExecStartTime()) + indent + indent + indent + dft.format(cloudlet.getFinishTime()) + indent + cloudlet.getUserId());

                    data[0] = ""+cloudlet.getCloudletId();
                    data[1] = "SUCCESS";
                    data[2] = ""+cloudlet.getResourceId();
                    data[3] = ""+cloudlet.getVmId();
                    data[4] = ""+dft.format(cloudlet.getActualCPUTime());
                    data[5] = ""+dft.format(cloudlet.getExecStartTime());
                    data[6] = ""+dft.format(cloudlet.getFinishTime());
                    data[7] = ""+cloudlet.getUserId();
                    writer.writeNext(data);
                }
            }
            writer.close();
        }
        catch (Exception e)
        {
            Log.printLine("Error Occoured"+e.toString());
        }

    }
}



