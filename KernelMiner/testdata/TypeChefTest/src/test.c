
#define __ARG_PLACEHOLDER_1 0,
#define config_enabled(cfg) defined(cfg)
//#define _config_enabled(value) __config_enabled(__ARG_PLACEHOLDER_##value)
//#define __config_enabled(arg1_or_junk) ___config_enabled(arg1_or_junk 1, 0)
//#define ___config_enabled(__ignored, val, ...) val

#define IS_ENABLED(option) (config_enabled(option) || config_enabled(option##_MODULE))

#define IS_BUILTIN(option) config_enabled(option)

#define IS_MODULE(option) config_enabled(option##_MODULE)

#if IS_BUILTIN(CONFIG_MAX)
	test
#else
	other test
#endif
