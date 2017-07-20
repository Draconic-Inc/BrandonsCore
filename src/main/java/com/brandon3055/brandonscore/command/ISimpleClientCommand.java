package com.brandon3055.brandonscore.command;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

public interface ISimpleClientCommand {

    void execute(ICommandSender sender, String[] args) throws CommandException;
}