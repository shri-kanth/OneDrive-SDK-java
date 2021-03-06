package com.bhyoo.onedrive.container.action;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import static lombok.AccessLevel.PRIVATE;

public class RenameAction {
	@Getter @Setter(PRIVATE) protected @NotNull String oldName;
}
