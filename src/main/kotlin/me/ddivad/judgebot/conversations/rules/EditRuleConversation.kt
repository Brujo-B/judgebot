package me.ddivad.judgebot.conversations.rules

import com.gitlab.kordlib.core.entity.Guild
import me.ddivad.judgebot.dataclasses.Configuration
import me.ddivad.judgebot.dataclasses.Rule
import me.ddivad.judgebot.services.DatabaseService
import me.jakejmattson.discordkt.api.arguments.BooleanArg
import me.jakejmattson.discordkt.api.arguments.EveryArg
import me.jakejmattson.discordkt.api.arguments.IntegerArg
import me.jakejmattson.discordkt.api.arguments.UrlArg
import me.jakejmattson.discordkt.api.dsl.Conversation
import me.jakejmattson.discordkt.api.dsl.conversation

class EditRuleConversation(private val configuration: Configuration,
                           private val databaseService: DatabaseService) : Conversation() {
    @Conversation.Start
    fun createAddRuleConversation(guild: Guild) = conversation {
        val rules = databaseService.guilds.getRules(guild)
        val ruleNumberToUpdate = promptMessage(IntegerArg, "Which rule would you like to update?")
        // TODO: rule embed for chosen rule here
        val ruleToUpdate = rules?.find { it.number == ruleNumberToUpdate }

        val updateNumber = promptMessage(BooleanArg(truthValue = "y", falseValue = "n"),
                "Update Rule number? (Y/N)")
        val ruleNumber = when {
            updateNumber -> promptUntil(
                    argumentType = IntegerArg,
                    prompt = "Please enter rule number:",
                    isValid = { number -> !rules?.any { it.number == number }!! },
                    error = "Rule with that number already exists"
            )
            else -> ruleToUpdate!!.number
        }
        val updateName = promptMessage(BooleanArg(truthValue = "y", falseValue = "n"),
                "Update Rule name? (Y/N)")
        val ruleName = when {
            updateName -> promptUntil(
                    EveryArg,
                    "Please enter rule name:",
                    "Rule with that name already exists",
                    isValid = { name -> !rules?.any { it.title == name }!! }
            )
            else -> ruleToUpdate!!.title
        }

        val updateText = promptMessage(BooleanArg(truthValue = "y", falseValue = "n"),
                "Update Rule text? (Y/N)")
        val ruleText = when {
            updateText -> promptMessage(EveryArg, "Please enter rule text:")
            else -> ruleToUpdate!!.description
        }

        val updateLink = promptMessage(BooleanArg(truthValue = "y", falseValue = "n"),
                "Update Rule link? (Y/N)")
        val ruleLink = when {
            updateLink -> promptMessage(UrlArg, "Please enter the link")
            else -> ruleToUpdate!!.link
        }

        val newRule = Rule(ruleNumber, ruleName, ruleText, ruleLink, ruleToUpdate!!.archived)
        databaseService.guilds.editRule(guild, ruleToUpdate, newRule)
        respond("Rule edited.")
    }
}