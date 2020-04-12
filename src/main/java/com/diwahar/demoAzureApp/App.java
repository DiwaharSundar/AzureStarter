package com.diwahar.demoAzureApp;

import java.io.File;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.NetworkInterface;
import com.microsoft.azure.management.network.PublicIPAddress;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.rest.LogLevel;

/**
 * Azure PlayGround
 *
 */
public class App {
	
	static Azure azure = null;
	
	public static void main(String[] args) {
		System.out.println("Hello World!");

		try {
			// cred file to access your Azure Account
			final File credFile = new File("C:\\Java\\demoAzureApp\\src\\main\\java\\com\\diwahar\\demoAzureApp\\azureauth.properties");
			
			// Getting Access to Azure Object
			azure = Azure.configure().withLogLevel(LogLevel.BASIC).authenticate(credFile)
					.withDefaultSubscription();
			
			//Your Azure Subscription Name
			System.out.println("Your Current Subscription: "+azure.getCurrentSubscription().displayName());
			
			// Get the instance of already running VM & Stopping it.
			VirtualMachine diwaNewVM = azure.virtualMachines().getByResourceGroup("DiwaAzResourceGroup", "DiwaNewVM");
			//diwaNewVM.startAsync();
			
			
			cloneAndStartVM(diwaNewVM, createNewNetworkInterface());
			System.out.println("VM created, Please check the portal");
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		//System.out.println("Creating resource group...");

	}
	
	
	private static NetworkInterface createNewNetworkInterface(){
		System.out.println("Creating public IP address...");
		PublicIPAddress publicIPAddress = azure.publicIPAddresses()
		    .define("myPublicIP")
		    .withRegion(Region.US_EAST)
		    .withExistingResourceGroup("DiwaAzResourceGroup")
		    .withDynamicIP()
		    .create();
		
		System.out.println("Creating Virtual Network...");
		Network network = azure.networks()
		    .define("myVN")
		    .withRegion(Region.US_EAST)
		    .withExistingResourceGroup("DiwaAzResourceGroup")
		    .withAddressSpace("10.0.0.0/16")
		    .withSubnet("mySubnet","10.0.0.0/24")
		    .create();
		
		System.out.println("Creating network interface...");
		NetworkInterface networkInterface = azure.networkInterfaces()
		    .define("myNIC")
		    .withRegion(Region.US_EAST)
		    .withExistingResourceGroup("DiwaAzResourceGroup")
		    .withExistingPrimaryNetwork(network)
		    .withSubnet("mySubnet")
		    .withPrimaryPrivateIPAddressDynamic()
		    .withExistingPrimaryPublicIPAddress(publicIPAddress)
		    .create();
		
		return networkInterface;
	}

	/**
	 *  Clone a new VM from an existing VM
	 * @param vm
	 */
	private static void cloneAndStartVM(VirtualMachine oldVM, NetworkInterface networkInterface) {
		System.out.println("Creating virtual machine...");		
		VirtualMachine vm = azure.virtualMachines()
				.define("DiwaNewVM2")
				.withRegion(oldVM.region())
				.withExistingResourceGroup(oldVM.resourceGroupName())
				.withExistingPrimaryNetworkInterface(networkInterface)
				.withLatestWindowsImage("MicrosoftWindowsServer", "WindowsServer", "2012-R2-Datacenter")
				.withAdminUsername("diwaharsundar")
				.withAdminPassword("Diwahar!03")
				.withComputerName("DiwaNewVM2")
				.withSize("Standard_B1ls")
				.create();
	}
	
	
	
	/**
	 * ShutDown VM 
	 * @param vm
	 */
	private static void shutDownVM(VirtualMachine vm) {
		vm.powerOff();
		System.out.println("Switched off VM - ["+vm.name()+"]");
	}
	
}
