from Faults import Memoryfault
from Faults import CpuFault
from Faults import KernelPanicFault
from Faults import ClockSkewFault
from Faults import DiskFillFault
from Faults import DiskIOFault
from Faults import KillProcessFault
from Faults import NetworkPartitionFault
from Faults import NetworkFault
from Faults import FileHandlerFault
from Faults import StopServiceFault
from Faults import PostgresConnectionLeakFault
from Faults import PostgresTransactionErrorFault
from Faults import PostgresTransactionLatencyFault
from Faults import MongodbConnectionLeakFault
from Faults import CassandraConnectionLeakFault


def get_fault(fault_args):
    fault_name = fault_args.get('--faultname')
    if fault_name == 'memoryFault':
        return Memoryfault.MemoryFault(fault_args)
    elif fault_name == 'cpuFault':
        return CpuFault.CpuFault(fault_args)
    elif fault_name == 'diskFault':
        return DiskIOFault.DiskIOFault(fault_args)
    elif fault_name == 'kernelPanicFault':
        return KernelPanicFault.KernelPanicFault(fault_args)
    elif fault_name == 'clockSkewFault':
        return ClockSkewFault.ClockSkewFault(fault_args)
    elif fault_name == 'diskSpaceFault':
        return DiskFillFault.DiskFillFault(fault_args)
    elif fault_name == 'killProcessFault':
        return KillProcessFault.KillProcessFault(fault_args)
    elif fault_name == 'networkPartitionFault':
        return NetworkPartitionFault.NetworkPartitionFault(fault_args)
    elif fault_name == 'networkFault':
        return NetworkFault.NetworkFault(fault_args)
    elif fault_name == 'fileHandlerFault':
        return FileHandlerFault.FileHandlerFault(fault_args)
    elif fault_name == 'stopServiceFault':
        return StopServiceFault.StopServiceFault(fault_args)
    elif fault_name == 'dbConnectionLeakFault_postgres':
        return PostgresConnectionLeakFault.PostgresConnectionLeakFault(fault_args)
    elif fault_name == 'dbTransactionErrorFault_postgres':
        return PostgresTransactionErrorFault.PostgresTransactionErrorFault(fault_args)
    elif fault_name == 'dbTransactionLatencyFault_postgres':
        return PostgresTransactionLatencyFault.PostgresTransactionLatencyFault(fault_args)
    elif fault_name == 'dbConnectionLeakFault_mongodb':
        return MongodbConnectionLeakFault.MongodbConnectionLeakFault(fault_args)
    elif fault_name == 'dbConnectionLeakFault_cassandra':
        return CassandraConnectionLeakFault.CassandraConnectionLeakFault(fault_args)
    else:
        print("Provided fault is not supported")
        return ValueError(fault_name)
