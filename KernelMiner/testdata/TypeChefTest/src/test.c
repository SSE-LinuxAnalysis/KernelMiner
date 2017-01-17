#ifdef CONFIG_ACPI
	#define ACPI_DEBUG_OUTPUT
#endif

#ifdef ACPI_DEBUG_OUTPUT
	#define ACPI_DEBUG_PARAMETERS valueParam1, valueParam2
	#define ACPI_DO_DEBUG_PRINT(param1, param2) param2
	#define ACPI_DEBUG_PRINT(plist) ACPI_DO_DEBUG_PRINT plist

#else
	#define ACPI_DEBUG_PRINT(pl)

#endif

int main() {
	ACPI_DEBUG_PRINT((ACPI_DEBUG_PARAMETERS));
	return 0;
}

// TypeChef params:
// --lex --no-analysis --prefixonly=CONFIG_ --output=testout test.c
