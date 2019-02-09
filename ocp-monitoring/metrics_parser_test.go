package main

import (
	"strings"
	"testing"
)
import . "github.com/smartystreets/goconvey/convey"

var metrics = `
# HELP health_signature_queue_depth_value Number of waiting messages
# TYPE health_signature_queue_depth_value gauge
health_signature_queue_depth_value 0.0
# HELP process_cpu_seconds_total Total user and system CPU time spent in seconds.
# TYPE process_cpu_seconds_total counter
process_cpu_seconds_total 166.027646
# HELP process_start_time_seconds Start time of the process since unix epoch in seconds.
# TYPE process_start_time_seconds gauge
process_start_time_seconds 1.54270537136E9
# HELP process_open_fds Number of open file descriptors.
# TYPE process_open_fds gauge
process_open_fds 377.0
# HELP process_max_fds Maximum number of open file descriptors.
# TYPE process_max_fds gauge
process_max_fds 10240.0
# HELP jvm_info JVM version info
# TYPE jvm_info gauge
jvm_info{version="1.8.0_161-b12",vendor="Oracle Corporation",runtime="Java(TM) SE Runtime Environment",} 1.0
# HELP jvm_classes_loaded The number of classes that are currently loaded in the JVM
# TYPE jvm_classes_loaded gauge
jvm_classes_loaded 27399.0
# HELP jvm_classes_loaded_total The total number of classes that have been loaded since the JVM has started execution
# TYPE jvm_classes_loaded_total counter
jvm_classes_loaded_total 27399.0
# HELP jvm_classes_unloaded_total The total number of classes that have been unloaded since the JVM has started execution
# TYPE jvm_classes_unloaded_total counter
jvm_classes_unloaded_total 0.0
# HELP jvm_gc_collection_seconds Time spent in a given JVM garbage collector in seconds.
# TYPE jvm_gc_collection_seconds summary
jvm_gc_collection_seconds_count{gc="PS Scavenge",} 27.0
jvm_gc_collection_seconds_sum{gc="PS Scavenge",} 1.659
jvm_gc_collection_seconds_count{gc="PS MarkSweep",} 7.0
jvm_gc_collection_seconds_sum{gc="PS MarkSweep",} 2.643
# HELP health_intygstjanst_accessible_normal 0 == OK 1 == NOT OK
# TYPE health_intygstjanst_accessible_normal gauge
health_intygstjanst_accessible_normal 1.0
# HELP jvm_threads_current Current thread count of a JVM
# TYPE jvm_threads_current gauge
jvm_threads_current 66.0
# HELP jvm_threads_daemon Daemon thread count of a JVM
# TYPE jvm_threads_daemon gauge
jvm_threads_daemon 38.0
# HELP jvm_threads_peak Peak thread count of a JVM
# TYPE jvm_threads_peak gauge
jvm_threads_peak 68.0
# HELP jvm_threads_started_total Started thread count of a JVM
# TYPE jvm_threads_started_total counter
jvm_threads_started_total 152.0
# HELP jvm_threads_deadlocked Cycles of JVM-threads that are in deadlock waiting to acquire object monitors or ownable synchronizers
# TYPE jvm_threads_deadlocked gauge
jvm_threads_deadlocked 0.0
# HELP jvm_threads_deadlocked_monitor Cycles of JVM-threads that are in deadlock waiting to acquire object monitors
# TYPE jvm_threads_deadlocked_monitor gauge
jvm_threads_deadlocked_monitor 0.0
# HELP health_db_accessible_normal 0 == OK 1 == NOT OK
# TYPE health_db_accessible_normal gauge
health_db_accessible_normal 0.0
# HELP health_jms_accessible_normal 0 == OK 1 == NOT OK
# TYPE health_jms_accessible_normal gauge
health_jms_accessible_normal 0.0
# HELP health_logged_in_users_value Current number of logged in users
# TYPE health_logged_in_users_value gauge
health_logged_in_users_value 5.0
# HELP health_privatlakarportal_accessible_normal 0 == OK 1 == NOT OK
# TYPE health_privatlakarportal_accessible_normal gauge
health_privatlakarportal_accessible_normal 1.0
# HELP jvm_memory_bytes_used Used bytes of a given JVM memory area.
# TYPE jvm_memory_bytes_used gauge
jvm_memory_bytes_used{area="heap",} 1.214814744E9
jvm_memory_bytes_used{area="nonheap",} 2.2997876E8
# HELP jvm_memory_bytes_committed Committed (bytes) of a given JVM memory area.
# TYPE jvm_memory_bytes_committed gauge
jvm_memory_bytes_committed{area="heap",} 1.891106816E9
jvm_memory_bytes_committed{area="nonheap",} 2.36978176E8
# HELP jvm_memory_bytes_max Max (bytes) of a given JVM memory area.
# TYPE jvm_memory_bytes_max gauge
jvm_memory_bytes_max{area="heap",} 3.817865216E9
jvm_memory_bytes_max{area="nonheap",} -1.0
# HELP jvm_memory_bytes_init Initial bytes of a given JVM memory area.
# TYPE jvm_memory_bytes_init gauge
jvm_memory_bytes_init{area="heap",} 2.68435456E8
jvm_memory_bytes_init{area="nonheap",} 2555904.0
# HELP jvm_memory_pool_bytes_used Used bytes of a given JVM memory pool.
# TYPE jvm_memory_pool_bytes_used gauge
jvm_memory_pool_bytes_used{pool="Code Cache",} 6.5569536E7
jvm_memory_pool_bytes_used{pool="Metaspace",} 1.4505216E8
jvm_memory_pool_bytes_used{pool="Compressed Class Space",} 1.9357064E7
jvm_memory_pool_bytes_used{pool="PS Eden Space",} 8.237958E8
jvm_memory_pool_bytes_used{pool="PS Survivor Space",} 6.4655224E7
jvm_memory_pool_bytes_used{pool="PS Old Gen",} 3.2636372E8
# HELP jvm_memory_pool_bytes_committed Committed bytes of a given JVM memory pool.
# TYPE jvm_memory_pool_bytes_committed gauge
jvm_memory_pool_bytes_committed{pool="Code Cache",} 6.7633152E7
jvm_memory_pool_bytes_committed{pool="Metaspace",} 1.49159936E8
jvm_memory_pool_bytes_committed{pool="Compressed Class Space",} 2.0185088E7
jvm_memory_pool_bytes_committed{pool="PS Eden Space",} 1.039138816E9
jvm_memory_pool_bytes_committed{pool="PS Survivor Space",} 1.96083712E8
jvm_memory_pool_bytes_committed{pool="PS Old Gen",} 6.55884288E8
# HELP jvm_memory_pool_bytes_max Max bytes of a given JVM memory pool.
# TYPE jvm_memory_pool_bytes_max gauge
jvm_memory_pool_bytes_max{pool="Code Cache",} 2.5165824E8
jvm_memory_pool_bytes_max{pool="Metaspace",} -1.0
jvm_memory_pool_bytes_max{pool="Compressed Class Space",} 1.073741824E9
jvm_memory_pool_bytes_max{pool="PS Eden Space",} 1.050673152E9
jvm_memory_pool_bytes_max{pool="PS Survivor Space",} 1.96083712E8
jvm_memory_pool_bytes_max{pool="PS Old Gen",} 2.863661056E9
# HELP jvm_memory_pool_bytes_init Initial bytes of a given JVM memory pool.
# TYPE jvm_memory_pool_bytes_init gauge
jvm_memory_pool_bytes_init{pool="Code Cache",} 2555904.0
jvm_memory_pool_bytes_init{pool="Metaspace",} 0.0
jvm_memory_pool_bytes_init{pool="Compressed Class Space",} 0.0
jvm_memory_pool_bytes_init{pool="PS Eden Space",} 6.7108864E7
jvm_memory_pool_bytes_init{pool="PS Survivor Space",} 1.1010048E7
jvm_memory_pool_bytes_init{pool="PS Old Gen",} 1.79306496E8
# HELP jvm_buffer_pool_used_bytes Used bytes of a given JVM buffer pool.
# TYPE jvm_buffer_pool_used_bytes gauge
jvm_buffer_pool_used_bytes{pool="direct",} 32862.0
jvm_buffer_pool_used_bytes{pool="mapped",} 0.0
# HELP jvm_buffer_pool_capacity_bytes Bytes capacity of a given JVM buffer pool.
# TYPE jvm_buffer_pool_capacity_bytes gauge
jvm_buffer_pool_capacity_bytes{pool="direct",} 32862.0
jvm_buffer_pool_capacity_bytes{pool="mapped",} 0.0
# HELP jvm_buffer_pool_used_buffers Used buffers of a given JVM buffer pool.
# TYPE jvm_buffer_pool_used_buffers gauge
jvm_buffer_pool_used_buffers{pool="direct",} 6.0
jvm_buffer_pool_used_buffers{pool="mapped",} 0.0
`

func TestParseMetrics(t *testing.T) {

	Convey("Given the metrics output", t, func() {

		Convey("When parsed using prometheus parser and our custom parser", func() {

			statuses := parseMetrics(strings.NewReader(metrics))

			Convey("Then we should have extracted 6 statuses", func() {
				So(len(statuses), ShouldEqual, 6)
				So(statuses[0].ServiceName, ShouldNotStartWith, "health_")
				So(statuses[0].ServiceName, ShouldNotEndWith, "_value")
				So(statuses[0].ServiceName, ShouldNotEndWith, "_normal")
			})
		})
	})
}
