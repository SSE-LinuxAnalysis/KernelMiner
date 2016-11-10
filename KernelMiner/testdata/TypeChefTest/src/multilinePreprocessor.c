
#define __ARG_PLACEHOLDER_1 0,
#define config_enabled(cfg) _config_enabled(cfg)
#define _config_enabled(value) __config_enabled(__ARG_PLACEHOLDER_##value)
#define __config_enabled(arg1_or_junk) ___config_enabled(arg1_or_junk 1, 0)
#define ___config_enabled(__ignored, val, ...) val

/*
 * IS_ENABLED(CONFIG_FOO) evaluates to 1 if CONFIG_FOO is set to 'y' or 'm',
 * 0 otherwise.
 *
 */
#define IS_ENABLED(option) \
	(config_enabled(option) || config_enabled(option##_MODULE))

/*
 * IS_BUILTIN(CONFIG_FOO) evaluates to 1 if CONFIG_FOO is set to 'y', 0
 * otherwise. For boolean options, this is equivalent to
 * IS_ENABLED(CONFIG_FOO).
 */
#define IS_BUILTIN(option) config_enabled(option)

/*
 * IS_MODULE(CONFIG_FOO) evaluates to 1 if CONFIG_FOO is set to 'm', 0
 * otherwise.
 */
#define IS_MODULE(option) config_enabled(option##_MODULE)

	
#undef CONFIG_SPLIT_PTLOCK_CPUS
#if !defined(CONFIG_MMU)
	#define CONFIG_SPLIT_PTLOCK_CPUS 999999
#endif
#if defined(CONFIG_MMU)
	#define CONFIG_SPLIT_PTLOCK_CPUS 4
#endif
	

#undef CONFIG_NR_CPUS
#if (defined(CONFIG_X86_32) && defined(CONFIG_SMP) && !defined(CONFIG_MAXSMP) && (!defined(CONFIG_SMP) || !defined(CONFIG_X86_BIGSMP)))
	#define CONFIG_NR_CPUS 8
#endif
#if (defined(CONFIG_MAXSMP) && defined(CONFIG_SMP))
	#define CONFIG_NR_CPUS 8192
#endif
#if (defined(CONFIG_SMP) && defined(CONFIG_X86_BIGSMP) && !defined(CONFIG_MAXSMP))
	#define CONFIG_NR_CPUS 32
#endif
#if !defined(CONFIG_SMP)
	#define CONFIG_NR_CPUS 1
#endif
#if (defined(CONFIG_SMP) && !defined(CONFIG_MAXSMP) && (!defined(CONFIG_SMP) || !defined(CONFIG_X86_BIGSMP)) && (!defined(CONFIG_X86_32) || !defined(CONFIG_SMP)))
	#define CONFIG_NR_CPUS 64
#endif

#define NR_CPUS CONFIG_NR_CPUS

#define USE_SPLIT_PTE_PTLOCKS	(NR_CPUS >= CONFIG_SPLIT_PTLOCK_CPUS)
#define USE_SPLIT_PMD_PTLOCKS	(USE_SPLIT_PTE_PTLOCKS && \
		IS_ENABLED(CONFIG_ARCH_ENABLE_SPLIT_PMD_PTLOCK))

#ifdef CONFIG_ARCH_ENABLE_SPLIT_PMD_PTLOCK
	hallo
#endif
		
int func() {
	if (USE_SPLIT_PMD_PTLOCKS) {
		return -1;
	} else {
		return 1;
	}
}

#if IS_ENABLED(CONFIG_MAX)
	test
#else
	other test
#endif

