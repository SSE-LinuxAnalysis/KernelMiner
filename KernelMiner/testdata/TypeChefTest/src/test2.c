#if defined(CONFIG_PM_SLEEP) & defined(CONFIG_ACPI_PROCESSOR_IDLE)
void acpi_processor_syscore_init(void);
void acpi_processor_syscore_exit(void);
#else
static inline void acpi_processor_syscore_init(void) {}
static inline void acpi_processor_syscore_exit(void) {}
#endif
