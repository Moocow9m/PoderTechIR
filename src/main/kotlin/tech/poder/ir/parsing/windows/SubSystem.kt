package tech.poder.ir.parsing.windows

enum class SubSystem(val id: Short) {
    UNKNOWN(0),
    NATIVE(1),
    WINDOWS_GUI(2),
    WINDOWS_CUI(3),
    OS2_CUI(5),
    POSIX_CUI(7),
    NATIVE_WINDOWS(8),
    WINDOWS_CE_GUI(9),
    EFI_APPLICATION(10),
    EFI_BOOT_SERVICE_DRIVER(11),
    EFI_RUNTIME_DRIVER(12),
    EFI_ROM(13),
    XBOX(14),
    WINDOWS_BOOT_APPLICATION(16)
}