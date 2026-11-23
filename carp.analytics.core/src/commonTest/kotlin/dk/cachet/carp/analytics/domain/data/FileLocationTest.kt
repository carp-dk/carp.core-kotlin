package dk.cachet.carp.analytics.domain.data

import kotlin.test.Test
import kotlin.test.assertEquals

class FileLocationTest
{
    @Test
    fun blank_output_path_takes_the_format_extension()
    {
        val resolved = FileLocation( format = FileFormat.HTML ).resolve( 6, "Map and timeline", "map-interactive" )

        assertEquals( "steps/07_map_and_timeline/outputs/map-interactive.html", resolved.path )
    }

    @Test
    fun blank_output_path_without_extension_has_no_trailing_dot()
    {
        val resolved = FileLocation( format = FileFormat.UNKNOWN ).resolve( 0, "Clean", "points" )

        assertEquals( "steps/01_clean/outputs/points", resolved.path )
    }
}
